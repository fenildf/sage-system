package sage.util;

import org.apache.commons.lang3.StringUtils;

/**
 * String utilities
 */
public class StringUtil {

  /**
   * Produce a substring. No matter if source is shorter than endIndex.
   */
  public static String cut(String source, int beginIndex, int endIndex) {
    if (endIndex < beginIndex) {
      throw new IllegalArgumentException(String.format("begin: %d, end: %d", beginIndex, endIndex));
    }
    if (endIndex > source.length()) {
      return source.substring(beginIndex);
    } else {
      return source.substring(beginIndex, endIndex);
    }
  }

  public static String omit(String source, int length) {
    if (source.length() > length) {
      return source.substring(0, length-3) + "...";
    } else {
      return source;
    }
  }

  public static String escapeHtmlTag(String text) {
    return StringUtils.replaceEach(text, new String[]{"&", "<", ">"}, new String[]{"&amp;", "&lt;", "&gt;"});
  }

  public static String escapeXmlInvalidChar(String text) {
    return text.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
  }
}
