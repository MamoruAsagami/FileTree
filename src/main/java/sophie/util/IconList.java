package sophie.util;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOImage;

public class IconList {
	
	private static class ICOImageComparator implements Comparator<ICOImage> {
		// Descending order of width/height and color depth;

		@Override
		public int compare(ICOImage image1, ICOImage image2) {
			int size1 = Math.max(image1.getWidth(), image1.getHeight());
			int size2 = Math.max(image2.getWidth(), image2.getHeight());
			return size1 != size2? size2 - size1: image2.getColourDepth() - image1.getColourDepth();
		}
	}

	public static List<Image> of(InputStream in) throws IOException {
		// For some reason, clazz.getResourceAsStream(...) sometimes doesn't work, but BufferedInputStream of it would do. 
		try(var bufferedIn = new BufferedInputStream(in)) {
			return ICODecoder.readExt(new BufferedInputStream(bufferedIn)).stream().sorted(new ICOImageComparator()).map(ICOImage::getImage).collect(Collectors.toList());
		}
	}
	
	public static List<Image> of(Class<?> clazz) throws IOException {
		try(InputStream in = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".ico")) {
			return of(in);
		}
	}
	public static List<Image> of(URL url) throws IOException {
		try(InputStream in = url.openStream()) {
			return of(in);
		}
	}
	
}
