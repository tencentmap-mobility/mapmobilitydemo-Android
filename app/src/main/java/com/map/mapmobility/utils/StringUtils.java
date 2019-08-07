package com.map.mapmobility.utils;

public class StringUtils {

    /**
     * 判断字符串是否为空、null、NULL、""
     * @param str
     * @return
     */
    public static boolean isJudgeEmpty(String str) {
        if(str != null && !"".equals(str) && !"null".equals(str) && !"NULL".equals(str)){
            return true;
        }
        return false;
    }

    /**
     * 判断两个字符串是否相等，并且都不为空
     */
    public static boolean isEqual(String str1, String str2) {
        if(isJudgeEmpty(str1) && isJudgeEmpty(str2)){
            if(str1.equals(str2)){
                return true;
            }
        }
        return false;
    }

    /**
     *  转换成字符串
     */
    public static String toStr(Object o){
        return o + "";
    }
}
