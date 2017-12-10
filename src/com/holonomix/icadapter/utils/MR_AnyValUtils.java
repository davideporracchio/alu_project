package com.holonomix.icadapter.utils;

import com.smarts.repos.MR_AnyValString;
import com.smarts.repos.MR_AnyValUnsignedInt;

/**
 * MR_AnyValUtils related utils
 */
public class MR_AnyValUtils {
	
    /**
    * Convenience routine to return a parameter as an MR_AnyValString
    * @param param the param to transform
    */
    public static MR_AnyValString mrAnyValString(Object param) {
        return new MR_AnyValString((String) param.toString());
    }

    /**
    * Convenience routine to return a parameter as an MR_AnyValUnsignedInt
    * @param param the param to transform
    */
    public static MR_AnyValUnsignedInt mrAnyValUnsignedInt(Object param) {
        return new MR_AnyValUnsignedInt((Long) new Long(param.toString()));
    }

}
