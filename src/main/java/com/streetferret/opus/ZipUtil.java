package com.streetferret.opus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
	public static void unzipKMLLayer(File from, File to) throws IOException {

		byte[] buffer = new byte[1024];

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(from))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().contains("PublicAccessLYR")) {
					try (FileOutputStream fos = new FileOutputStream(to)) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}

				}
				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
		}
	}

	public static void unzipKML(File from, File to) throws IOException {

		byte[] buffer = new byte[1024];

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(from))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName().equals("doc.kml")) {
					try (FileOutputStream fos = new FileOutputStream(to)) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}
				}
				zis.closeEntry();
				zipEntry = zis.getNextEntry();
			}
		}
	}
}
