package com.holonomix.icadapter.utils;



/**
 * EncodeUtil html encoding related utils
 */
public class EncodeUtil {
	
    private static final char invalidXML[] = {'&', '<', '>', '\"', '\'' };
    private static final String escapedXML[] = {"&amp;", "&lt;", "&gt;", "&quot;", "&quot;"};
    /**
    * html encode a string
    * @param str the string to transform
    */
    public static String htmlEncode(String str) {
        if (str == null || "".equals(str))
                return null;

        char chars[] = str.toString().toCharArray();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '&': 
                    sb.append("&amp;");
                    break;
                case '<': 
                    sb.append("&lt;");
                    break;
                case '>': 
                    sb.append("&gt;");
                    break;
                case '\"': 
                    sb.append("&quot;");
                    break;
                case '\'': 
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    /**
    * inchargeObjectName name encode a string. Basically prepend the
    * following special chars with an "_": <!> to </>; <:> to <@>; and <[>
    * to <^>
    * @param str the string to transform
    */
    public static String inchargeObjectNameEncode(String str) {
        if (str == null || "".equals(str))
                return null;

        char chars[] = str.toString().toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case ' ': 
                    sb.append("_20");
                    break;
                case ':': 
                    sb.append("_:");
                    break;
                case '_': 
                    sb.append("__");
                    break;
                default:
                    sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
}
