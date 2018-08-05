package beidanci.controller;

import java.io.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.User;
import beidanci.util.SysParamUtil;
import beidanci.util.Util;
import beidanci.vo.BatchImportResult;

@Controller
public class BatchImportRawWords {
	private static Logger log = LoggerFactory.getLogger(BatchImportRawWords.class);

	@RequestMapping("/batchImportRawWords.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		Util.setPageNoCache(response);

		String userName = request.getParameter("userName");
		User user = Global.getUserBO().findById(userName);

		try {
			BatchImportResult result = null;
			if (ServletFileUpload.isMultipartContent(request)) {
				DiskFileItemFactory dff = new DiskFileItemFactory();// 创建该对象
				dff.setRepository(new File(SysParamUtil.getTempDirForUpload()));// 指定上传文件的临时目录
				dff.setSizeThreshold(1024 * 1024);// 指定在内存中缓存数据大小,单位为byte
				ServletFileUpload sfu = new ServletFileUpload(dff);// 创建该对象
				sfu.setFileSizeMax(2 * 1024 * 1024);// 指定单个上传文件的最大尺寸
				sfu.setSizeMax(2 * 1024 * 1024);// 指定一次上传多个文件的总尺寸
				FileItemIterator fii = sfu.getItemIterator(request);// 解析request请求,并返回FileItemIterator集合

				while (fii.hasNext()) {
					FileItemStream fis = fii.next();// 从集合中获得一个文件流
					if (!fis.isFormField() && fis.getName().length() > 0) {// 过滤掉表单中非文件域
						String fileName = fis.getName();// 获得上传文件的文件名
						BufferedInputStream in = new BufferedInputStream(fis.openStream());// 获得文件输入流
						File targetFile = new File(SysParamUtil.getSaveDirForUpload() + "/" + fileName);
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));// 获得文件输出流
						Streams.copy(in, out, true);// 开始把文件写到你指定的上传文件夹

						// 导入文件中的生词
						result = batchImport(targetFile, user);
						break;// 目前只支持上传一个文件
					}
				}
			}

			// Send result back to client.
			Util.sendBooleanResponse(true, "导入成功", result, response);

		} catch (Exception e) {
			log.error("", e);
			Util.sendBooleanResponse(false, "导入失败! 原因：系统故障", null, response);
		}

		return null;
	}

	/**
	 * 从上传的文件中批量导入生词
	 * 
	 * @param rawWordFile
	 * @throws IOException
	 * @throws EmptySpellException
	 * @throws InvalidMeaningFormatException
	 * @throws ParseException
	 */
	private BatchImportResult batchImport(File rawWordFile, User user)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {

		int successCount = 0;
		int failedCount = 0;
		int ignoredCount = 0;
		List<String> failedWords = new LinkedList<String>();
		List<String> failedReasons = new LinkedList<String>();

		// 根据文件格式创建相应的Reader
		BatchFileReader reader = null;
		if (rawWordFile.getName().endsWith(".txt")) {
			reader = new TextBatchFileReader(rawWordFile);
		}
		if (rawWordFile.getName().endsWith(".xml")) {
			reader = new YouDaoXmlBatchFileReader(rawWordFile);
		}

		for (String wordStr : reader) {
			if (wordStr == null) {
				continue;
			}

			String spell = wordStr.trim();

			if (spell.length() > 0) {
				String errMsg = AddRawWord.addRawWord(spell, user, "批量导入");
				if (errMsg == null) {
					successCount++;
				} else if (errMsg.equals(AddRawWord.WORD_ALREADY_IN_RAW_WORD_BOOK)) {
					ignoredCount++;
				} else {
					failedCount++;
					failedWords.add(spell);
					failedReasons.add(errMsg);
					log.info(String.format("单词[%s]未导入", spell));
				}
			}
		}

		BatchImportResult result = new BatchImportResult();
		result.setSuccessCount(successCount);
		result.setFailedCount(failedCount);
		result.setIgnoredCount(ignoredCount);
		result.setFailedWords(failedWords.toArray(new String[0]));
		result.setFailedReasons(failedReasons.toArray(new String[0]));

		log.info(String.format("导入生词本成功，导入[%d]词，[%d]词未导入", successCount, failedCount));

		return result;
	}

	private interface BatchFileReader extends Iterable<String> {
	}

	private class YouDaoXmlBatchFileReader implements BatchFileReader {
		private File xmlFile;

		public YouDaoXmlBatchFileReader(File xmlFile) {
			this.xmlFile = xmlFile;
		}

		@Override
		public Iterator<String> iterator() {
			try {
				return new Ita();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private class Ita implements Iterator<String> {
			private XmlParser xmlParser;

			public Ita() throws ParserConfigurationException, SAXException, IOException {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				final SAXParser saxParser = spf.newSAXParser();
				xmlParser = new XmlParser();
				new Thread() {
					public void run() {
						try {
							saxParser.parse(xmlFile, xmlParser);
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				}.start();
			}

			@Override
			public boolean hasNext() {
				return xmlParser.hasNext();
			}

			@Override
			public String next() {
				while (xmlParser.hasNext()) {
					String str = xmlParser.getNext();
					if (str != null) {
						return str;
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			private class XmlParser extends DefaultHandler {
				private BlockingQueue<String> words = new LinkedBlockingQueue<String>(1);
				private boolean parseFinished = false;
				private boolean isWordTag = false; // 当前Tag是否是"word"

				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
					isWordTag = qName.equals("word");
				}

				public void endDocument() throws SAXException {
					parseFinished = true;
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					if (isWordTag) {
						String word = new String(ch, start, length).trim();
						if (word.length() > 0) {
							try {
								words.put(word);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							log.info(String.format("start[%d] length[%d] %s", start, length,
									new String(ch, start, length)));
						}

					}
				}

				public boolean hasNext() {
					return !parseFinished || words.size() > 0;
				}

				public String getNext() {
					try {
						return words.poll(100, TimeUnit.MICROSECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}
	}

	private class TextBatchFileReader implements BatchFileReader {
		private File textFile;

		public TextBatchFileReader(File textFile) {
			this.textFile = textFile;
		}

		@Override
		public Iterator<String> iterator() {

			return new Ita();

		}

		private class Ita implements Iterator<String> {

			private String currentLine;
			BufferedReader reader;

			public Ita() {
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean hasNext() {
				try {
					currentLine = reader.readLine();
					if (currentLine == null) {
						reader.close();
					}
					return currentLine != null;
				} catch (IOException e) {
					e.printStackTrace();
					try {
						reader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return false;
				}
			}

			@Override
			public String next() {
				return currentLine;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		}

	}
}
