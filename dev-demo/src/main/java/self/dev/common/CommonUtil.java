package self.dev.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    public static byte[] int2ByteArray(int count) {
        byte[] byteArr = new byte[4];
        byteArr[3] = ((byte)(count & 0xFF));// 低位
        byteArr[2] = ((byte)(count >> 8 & 0xFF));
        byteArr[1] = ((byte)(count >> 16 & 0xFF));
        byteArr[0] = ((byte)(count >> 24 & 0xFF));// 高位
        return byteArr;
    }

    public static int byteArray2int(byte[] byteArr){
        int count = 0;
        for (int i = 0; i < 4; i++){
            count <<= 8;
            count |= byteArr[i] & 0xFF;
        }
        return count;
    }

    public static boolean isNum(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
