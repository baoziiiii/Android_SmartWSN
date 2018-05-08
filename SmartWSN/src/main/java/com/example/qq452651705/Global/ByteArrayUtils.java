package com.example.qq452651705.Global;

/**
 * Created by B on 2018/2/21.
 */
/**
 *  字符数组转换类（BLE相关）
 */
public class ByteArrayUtils {
    /**
     * 将4字节的byte数组转成一个int值
     * @param
     * @return
     */
    public static int byteArrayToInt(byte[] b){
        byte[] a = new byte[4];
        int i = a.length - 1,j = b.length - 1;
        for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
            if(j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff) ;
        return v0 + v1 + v2 + v3;
    }
    /**
     * 通过byte数组取得float
     *
     * @param
     * @param
     * @return
     */
    public static float getFloat(byte[] b) {
        int l;
        l = b[3];
        l &= 0xff;
        l |= ((long) b[2] << 8);
        l &= 0xffff;
        l |= ((long) b[1] << 16);
        l &= 0xffffff;
        l |= ((long) b[0] << 24);
        return Float.intBitsToFloat(l);
    }
    /**
     * 通过byte数组取得float
     *
     * @param
     * @param
     * @return
     */
    public static double getDouble(byte[] b) {
        long l;
        l = b[0];
        l &= 0xff;
        l |= ((long) b[1] << 8);
        l &= 0xffff;
        l |= ((long) b[2] << 16);
        l &= 0xffffff;
        l |= ((long) b[3] << 24);
        l &= 0xffffffffl;
        l |= ((long) b[4] << 32);
        l &= 0xffffffffffl;
        l |= ((long) b[5] << 40);
        l &= 0xffffffffffffl;
        l |= ((long) b[6] << 48);
        l &= 0xffffffffffffffl;
        l |= ((long) b[7] << 56);
        return Double.longBitsToDouble(l);
    }

    public static byte[] intToByteArrya(Integer i){
        byte[] bytes=new byte[4];
        bytes[0] = (byte) ((i >> 24) & 0xff);
        bytes[1] = (byte) ((i >> 16) & 0xff);
        bytes[2] = (byte) ((i >> 8) & 0xff);
        bytes[3] = (byte) (i & 0xff);
        return bytes;
    }
}
