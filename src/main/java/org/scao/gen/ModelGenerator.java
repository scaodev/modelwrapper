package org.scao.gen;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Date: 7/10/14
 * Time: 7:41 PM
 */
public class ModelGenerator {
	private final File targetPath;

	public ModelGenerator(File targetPath) {
		this.targetPath = targetPath;
	}

	public static void main(String args[]) {
		List<String> vals = Arrays.asList("hello", "world");
		vals.forEach((final String s ) -> System.out.println(s));
	}

	public static class Builder{
		private File targetPath;

		public Builder setTargetPath(File targetPath) {
			this.targetPath = targetPath;
		}

		public ModelGenerator build() {
			return new ModelGenerator(targetPath);
		}
	}
}
