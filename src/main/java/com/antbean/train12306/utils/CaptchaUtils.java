package com.antbean.train12306.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Positions;

public final class CaptchaUtils {
	public static File[] slice(File captchaFile) {
		// 293 * 190
		try {
			File[] fs = new File[11];
			// 存放路径
			File dir = captchaFile.getParentFile();
			// 保存名称
			String name = captchaFile.getName();
			// 扩展名
			String expandedName = "";
			int pIdx;
			if ((pIdx = name.lastIndexOf(".")) != -1) {
				expandedName = name.substring(pIdx, name.length());
				name = name.substring(0, pIdx);
			}

			fs[0] = new File(dir, name + "_t" + expandedName);
			fs[1] = new File(dir, name + "_t1" + expandedName);
			fs[2] = new File(dir, name + "_t2" + expandedName);
			Thumbnails.of(captchaFile).sourceRegion(Positions.TOP_CENTER, 293, 30).size(293, 30).toFile(fs[0]);
			Thumbnails.of(captchaFile).sourceRegion(new Coordinate(0, 0), 120, 30).size(120, 30).toFile(fs[1]);
			Thumbnails.of(captchaFile).sourceRegion(new Coordinate(120, 0), 173, 30).size(173, 30).toFile(fs[2]);

			for (int i = 0; i < 8; i++) {
				int x = (int) (i % 4 * 73.25);
				int y = (int) (i / 4 * 80) + 30;

				fs[3 + i] = new File(dir, name + "_" + i + expandedName);
				Thumbnails.of(captchaFile).sourceRegion(new Coordinate(x, y), 73, 80).size(73, 80).toFile(fs[3 + i]);
			}
			return fs;
		} catch (IOException e) {
			throw new RuntimeException("切割验证码失败", e);
		}
	}

	public static void attack(int count) {
		String name = UUID.randomUUID().toString();
		File cacheDir = getCacheDir();
		File captchaFile = new File(cacheDir, name + ".jpg");
		// 从12306下载验证码到文件
		Train12306HttpUtils.writeCaptcha(captchaFile);
		System.out.println(">>>>>>>>>>>> 已下载验证码" + captchaFile.getAbsolutePath());
		// 切割验证码
		File[] sliceFiles = slice(captchaFile);
		// 识别文字
		String[] words = WordRecognitionUtils.getWordsFromImg(sliceFiles[2].getAbsolutePath());
		if (words.length == 0) {
			throw new RuntimeException("未识别出验证码");
		}
		System.out.println(">>>>>>>>>>>> 识别出的文字：" + Arrays.toString(words));
		// 识别图片信息
		String[] infos = new String[8];
		for (int i = 0; i < infos.length; i++) {
			try {
				infos[i] = ImageRecognitionUtils.recognitionImg(sliceFiles[3 + i]);
			} catch (Exception e) {
				infos[i] = StringUtils.EMPTY;
			}
		}
		System.out.println(">>>>>>>>>>>> 识别出的图片信息：" + Arrays.toString(infos));
		// 找文字对应的图片
		List<Integer> attackPoints = findAttackPoints(words, infos);
		if (0 == attackPoints.size()) {
			throw new RuntimeException("没有找到文字对应的图片");
		}
		System.out.println(">>>>>>>>>>>> 找到的图片点：" + attackPoints);
		String coordinateString = getCoordinateString(attackPoints);
		System.out.println(">>>>>>>>>>>> 图片坐标：" + coordinateString);
		// 到服务端校验验证码
		Train12306HttpUtils.checkCaptcha(coordinateString);
		System.out.println(">>>>>>>>>>>>>>>>> 验证码校验成功!!!");
	}

	private static String getCoordinateString(List<Integer> points) {
		StringBuffer buff = new StringBuffer();
		for (Integer point : points) {
			int x = (int) (point % 4 * 73.25) + 40;
			int y = (int) (point / 4 * 80) + 30 + 40;
			buff.append("," + x + "," + y);
		}
		return buff.substring(1).toString();
	}

	private static List<Integer> findAttackPoints(String[] words, String[] infos) {
		StringBuffer buff = new StringBuffer();
		for (String w : words) {
			buff.append(w);
		}
		String w = buff.toString();
		List<Integer> aPoints = new ArrayList<>();
		for (int j = 0; j < infos.length; j++) {
			String info = infos[j];
			int similarity = getSimilarity(w, info);
			if (similarity > 0) {
				aPoints.add(j);
			}
		}
		return aPoints;
	}

	private static int getSimilarity(String words, String info) {

		Set<Character> set1 = new HashSet<Character>();
		for (int i = 0; i < words.length(); i++) {
			set1.add(words.charAt(i));
		}
		Set<Character> set2 = new HashSet<Character>();
		for (int i = 0; i < info.length(); i++) {
			set2.add(info.charAt(i));
		}
		Set<Character> set = new HashSet<>();
		set.addAll(set1);
		set.addAll(set2);
		return set1.size() + set2.size() - set.size();
	}

	public static void main(String[] args) {
		// File dir = new File("C:\\Users\\work0401\\Desktop\\12306验证码");
		// // 下载100个验证码
		// for (int i = 0; i < 10; i++) {
		// File captchaFile = new File(dir, "captcha_" + i + ".jpg");
		// try {
		// Train12306HttpUtils.writeCaptcha(captchaFile);
		// } catch (Exception e) {
		// System.err.println("下载验证码失败:" + i);
		// continue;
		// }
		// try {
		// slice(captchaFile);
		// } catch (Exception e) {
		// System.err.println("切割验证码失败:" + i);
		// continue;
		// }
		//
		// try {
		// TimeUnit.SECONDS.sleep(3);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

		// System.out.println(new File("E:\\test\\1.jpg").getAbsolutePath());
		attack(0);
	}

	private static File getCacheDir() {
		String s = ClassLoader.getSystemClassLoader().getResource("12306/captcha0/1.txt").getFile();
		return new File(s.substring(1)).getParentFile();
	}

}
