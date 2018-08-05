package beidanci.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import beidanci.exception.InvalidWordTypeException;
import beidanci.store.CiXing;
import beidanci.vo.MeaningItemVo;
import beidanci.vo.SentenceVo;
import beidanci.vo.WordVo;

/**
 * 从有道导入更完整的单词信息以完善词库，以现有词库为驱动。
 * <p/>
 * 从有道导入的新版本的单词将被保存到一个单独文件，需用其他工具导入词库
 *
 * @author Administrator
 */
public class YoudaoWordImporter {
	private static final Logger log = Logger.getLogger(YoudaoWordImporter.class);

	/**
	 * 从有道词典网页抓取指定单词对应的网页并将其解析为一个Word对象<b> 边际效应：会同时下载单词的发音文件并放入单词语音库（如果语音库中没有该文件）
	 *
	 * @param spell
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static WordVo getWordFromYoudao(final String spell, final String soundPath)
			throws UnsupportedEncodingException, IOException {
		// 从有道网站过去该单词的网页内容
		String spell2 = spell.replaceAll("\\(", " ").replaceAll("\\)", " ");
		String url = "http://dict.youdao.com/search?le=eng&q=" + spell2 + "&keyfrom=dict.top";
		final String html = Util.getHtml(url, "UTF-8", "UTF-8", false);
		Document doc = Jsoup.parse(html);

		// 获取单词拼写
		String wordSpell = doc.select("span.keyword").text();
		if (!wordSpell.equals(spell2)) {
			log.warn(String.format("Get word (%s) from YouDao, but it returns: %s", spell2, wordSpell));
		}

		if (wordSpell.equals("")) {
			return null;
		}

		WordVo word = new WordVo(wordSpell);

		// 获取单词音标
		Elements pronounces = doc.select("div.baav span.phonetic");
		if (pronounces != null) {
			if (pronounces.size() == 2) {
				String british = pronounces.first().text();
				String american = pronounces.last().text();
				word.setBritishPronounce(british);
				word.setAmericaPronounce(american);
			} else if (pronounces.size() > 0) {
				String pronounce = pronounces.first().text();
				word.setPronounce(pronounce);
			}
		}

		// 获取单词释义, 如果找不到“trans-container”标记，则只好从网络释义中提取单词释义
		int start = html.indexOf("<div class=\"trans-container\">");
		if (start == -1) {// 从网络释义中提取
			/*
			 * start = html.indexOf("<div id=\"tWebTrans\""); String meaning =
			 * Util.getXmlTagContent("span", html, start); meaning =
			 * Util.uniformString(Util.deleteAllXmlTag(meaning)); word.addMeaningItem(new
			 * MeaningItemVo(null, meaning));
			 */

			// 由于网络释义质量不可靠，所以屏蔽掉
			return null;

		} else {
			String tag = Util.getXmlTagContent("div", html, start);
			tag = Util.getXmlTagContent("ul", tag, 0);
			if (tag != null) {
				start = tag.indexOf("<li>");
				String meaningStr = Util.getXmlTagContent("li", tag, start);
				while (meaningStr != null) {
					// 把释义分解为词性和意义两部分
					boolean hasCiXing = false;
					String[] parts = meaningStr.split("\\.");
					if (parts.length > 1) {
						try {
							CiXing.parse(parts[0] + ".");
							hasCiXing = true;
						} catch (InvalidWordTypeException e) {
							hasCiXing = false;
						}
					}

					if (hasCiXing) {
						word.addMeaningItem(
								new MeaningItemVo(parts[0] + ".", Utils.uniformString(Util.deleteAllXmlTag(parts[1]))));
					} else {
						word.addMeaningItem(
								new MeaningItemVo(null, Utils.uniformString(Util.deleteAllXmlTag(meaningStr))));
					}

					start += meaningStr.length() + "<li></li>".length();
					meaningStr = Util.getXmlTagContent("li", tag, start);
				}
			}
		}

		// 获取单词例句
		Elements sentenceElements = doc.select("div#originalSound li");
		for (Element sentenceElement : sentenceElements) {
			String english = null;
			String chinese = null;
			String soundUrl = null;
			int pIndex = 0;
			for (Element p : sentenceElement.select("p")) {
				if (pIndex == 0) {
					english = p.text();
					boolean isHumanVoice = p.select("a.humanvoice").size() > 0;
					if (isHumanVoice) {
						soundUrl = p.select("a.humanvoice").attr("data-rel");
					}
				} else if (pIndex == 1 && !p.hasAttr("class")) {
					chinese = p.text();
				}
				pIndex++;
			}
			if (soundUrl != null) {
				String englishDigest = Util.makeSentenceDigest(english);
				word.getSentences().add(new SentenceVo(null, english, chinese, "human_audio", englishDigest, null));
				File mp3File = new File(soundPath + "/sentence/" + englishDigest + ".mp3");
				Util.downloadFile(new URL(soundUrl), mp3File);
			}
		}

		// 下载单词的发音文件(如果该单词没有发音文件)
		File mp3File = new File(soundPath + "/" + Utils.getFileNameOfWordSound(spell) + ".mp3");
		if (!mp3File.exists()) {
			Util.println("Downloading sound file of word: " + spell + " ...");
			Util.downloadFile(new URL("http://dict.youdao.com/dictvoice?audio=" + spell2 + "&type=2"), mp3File);
		}

		return word;
	}

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		WordVo word = getWordFromYoudao("dropping", "d:/Sound");
		System.out.println(word);
	}

}
