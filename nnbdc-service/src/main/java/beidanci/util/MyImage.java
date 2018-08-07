package beidanci.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FileUtils;

import com.mortennobel.imagescaling.ResampleOp;

/**
 * 图片缩放工具类
 *
 * @author sunnymoon
 */
public class MyImage {
	/**
	 * 接收File输出图片
	 *
	 * @param file
	 * @param writePath
	 * @param targetWidth
	 * @param targetHeight
	 * @param format
	 * @return
	 * @throws IOException
	 */
	public static void resizeImage(File srcFile, File targetFile, Integer targetWidth, Integer targetHeight,
			String format) throws IOException {
		BufferedImage inputBufImage = ImageIO.read(srcFile);
		// 如果原图片尺寸和格式都与目标相同，则不需要转换
		if (inputBufImage.getWidth() == targetWidth && inputBufImage.getHeight() == targetHeight
				&& getImageFormat(srcFile).equalsIgnoreCase(format)) {
			FileUtils.copyFile(srcFile, targetFile);
			return;
		}

		// create a blank, RGB, same width and height, and a white background
		BufferedImage newBufferedImage = new BufferedImage(inputBufImage.getWidth(), inputBufImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		newBufferedImage.createGraphics().drawImage(inputBufImage, 0, 0, Color.WHITE, null);

		ResampleOp resampleOp = new ResampleOp(targetWidth, targetHeight);// 转换
		BufferedImage rescaledTomato = resampleOp.filter(newBufferedImage, null);
		ImageIO.write(rescaledTomato, format, targetFile);
	}

	public static byte[] readBytesFromIS(InputStream is) throws IOException {
		int total = is.available();
		byte[] bs = new byte[total];
		is.read(bs);
		return bs;
	}

	public static String getImageFormat(File imageFile) throws IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
		try {
			for (Iterator<ImageReader> i = ImageIO.getImageReaders(iis); i.hasNext();) {
				ImageReader reader = i.next();
				return reader.getFormatName();
			}
			return null;
		} finally {
			iis.close();
		}
	}

	// 测试：只测试了字节流的方式，其它的相对简单，没有一一测试
	public static void main(String[] args) throws IOException {
		int width = 470;
		int height = 470;
		File inputFile = new File("F:\\p681.jpg");
		System.out.printf("src format name: %s%n", getImageFormat(inputFile));
		MyImage.resizeImage(inputFile, new File("F:\\to.jpg"), width, height, "JPEG");
	}
}