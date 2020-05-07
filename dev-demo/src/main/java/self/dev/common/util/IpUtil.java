package self.dev.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import self.dev.common.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpUtil {
	private static final int DEFAULT_MASK = 24;

	private static final String[] headers = {
			"X-Forwarded-For",
			"Proxy-Client-IP",
			"WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR",
			"HTTP_X_FORWARDED",
			"HTTP_X_CLUSTER_CLIENT_IP",
			"HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR",
			"HTTP_FORWARDED",
			"HTTP_VIA",
			"REMOTE_ADDR",
			"X-Real-IP"
	};

	/**
	 * 获取请求IP
	 * @param request
	 * @return
	 */
	public static String getRequestIp(HttpServletRequest request) {
		String ip = null;

		for (String header : headers) {
			ip = request.getHeader(header);
			if (!ipIsBlank(ip)) {
				break;
			}
		}

		if (ipIsBlank(ip)) {
			ip = request.getRemoteAddr();
		}

		if (!(ipIsBlank(ip))) {
			String[] ips = ip.split(",");
			if(ips.length > 0){
				ip = ips[0];
			}

			if("0:0:0:0:0:0:0:1".equals(ip)){
				ip = "127.0.0.1";
			}
		}

		return ip;
	}
	private static boolean ipIsBlank(String ip){
		return (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip));
	}

	/**
	 * 获取指定网卡的IP
	 * @param netCardName 网卡名称
	 * @return
	 */
	public static String getNetCardIp(String netCardName) {
		String ip = null;
		try{
			Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaceEnumeration.hasMoreElements()) {
				if(ip != null){
					break;
				}
				NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
				String networkInterfaceName = networkInterface.getName();
				if(!networkInterfaceName.equalsIgnoreCase(netCardName)) {
					continue;
				}

				Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
				while (inetAddressEnumeration.hasMoreElements()) {
					InetAddress inetAddress = inetAddressEnumeration.nextElement();

					if (inetAddress.isLoopbackAddress()) {
						ip = "127.0.0.1";
						break;
					}

					if(inetAddress instanceof Inet4Address){
						ip = inetAddress.getHostAddress();
						break;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	/**
	 * 获取主机IP
	 * @return
	 */
	public static String getLocalHostIp() {
		String localHostIp = null;
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while(netInterfaces.hasMoreElements()) {
				if(localHostIp != null){
					break;
				}
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ip = ips.nextElement();
					if( ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == 1){
						localHostIp = ip.getHostAddress();
						break;
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return localHostIp;
	}

	/**
	 * 通过IP获取地址信息
	 * @param request
	 * @return
	 */
	public static String getIpInfo(HttpServletRequest request) {
		String info = "";
		InputStream in = null;
		InputStreamReader inReader = null;
		BufferedReader bufferedReader = null;
		try {
			URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip=" + IpUtil.getRequestIp(request));
			HttpURLConnection htpcon = (HttpURLConnection) url.openConnection();
			htpcon.setRequestMethod("GET");
			htpcon.setDoOutput(true);
			htpcon.setDoInput(true);
			htpcon.setUseCaches(false);

			in = htpcon.getInputStream();
			inReader = new InputStreamReader(in);
			bufferedReader = new BufferedReader(inReader);

			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}

			JSONObject result = (JSONObject) JSON.parse(temp.toString());
			if (result.getIntValue("code") == 0) {
				JSONObject data = result.getJSONObject("data");
				info += data.getString("country") + " ";
				info += data.getString("region") + " ";
				info += data.getString("city") + " ";
				info += data.getString("isp");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(inReader != null){
				try {
					inReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return info;
	}

	/**
	 * 验证IP是否合法
	 * @param ip
	 * @return
	 */
	public static boolean verifyIp(String ip) {
		Pattern pattern = Pattern
				.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher m = pattern.matcher(ip);
		return m.matches();
	}

	/**
	 * 判断两个ip是否属于同一网段
	 * @param ip1
	 * @param ip2
	 * @param mask 子网掩码
	 * @return
	 */
	private static boolean checkSameSegment(String ip1, String ip2, int mask) {
		if (!verifyIp(ip1)) {
			throw new RuntimeException("ip=["+ip1+"]不合法!");
		}
		if (!verifyIp(ip2)) {
			throw new RuntimeException("ip=["+ip2+"]不合法!");
		}
		int ipValue1 = getIpV4Value(ip1);
		int ipValue2 = getIpV4Value(ip2);
		return ((mask & ipValue1) == (mask & ipValue2));
	}

	/**
	 * 判断两个ip是否属于同一网段
	 * @param ip1
	 * @param ip2
	 * @return
	 */
	private static boolean checkSameSegment(String ip1, String ip2) {
		return checkSameSegment(ip1, ip2, DEFAULT_MASK);
	}
	private static int getIpV4Value(String ipOrMask) {
		byte[] addr = getIpV4Bytes(ipOrMask);
		int address1 = addr[3] & 0xFF;
		address1 |= ((addr[2] << 8) & 0xFF00);
		address1 |= ((addr[1] << 16) & 0xFF0000);
		address1 |= ((addr[0] << 24) & 0xFF000000);
		return address1;
	}
	private static byte[] getIpV4Bytes(String ipOrMask) {
		String[] addrs = ipOrMask.split("\\.");
		int length = addrs.length;
		byte[] addr = new byte[length];
		for (int index = 0; index < length; index++) {
			addr[index] = (byte) (Integer.parseInt(addrs[index]) & 0xFF);
		}
		return addr;
	}

	/**
	 * 比较ip地址是否在某网段内
	 * @param ip
	 * @param ipOrMask
	 * @return
	 */
	public static boolean isIncludeIp(String ip, String ipOrMask){
		boolean flag = false;
		if(ipOrMask.contains(",")){
			String[] ips = ipOrMask.split(",");
			for(String s : ips){
				flag = checkIpField(ip, s);
				if(flag){
					break;
				}
			}
		}else{
			flag = checkIpField(ip, ipOrMask);
		}
		return flag;
	}
	public  static boolean checkIpField(String ip1, String ip2Mask){
		int idex = ip2Mask.indexOf("/");
		if(idex == -1){
			if(!verifyIp(ip2Mask)){
				throw new RuntimeException("ip=["+ip2Mask+"]不合法!");
			}
			return ip1.equals(ip2Mask);
		}
		String maskStr = ip2Mask.substring(idex + 1);
		if(StringUtils.isEmpty(maskStr)){
			return false;
		}

		int mask = Integer.parseInt(maskStr);
		String ip2 = ip2Mask.substring(0, idex);
		return checkSameSegment(ip1, ip2, getMaskInt(mask));
	}
	private static int getMaskInt(int maskLength) {
		int subLength = (32 - maskLength);
		int r = 0;
		for (int i=0; i<subLength; i++) {
			r += (1 << i);
		}
		return ~r;
	}

	/**
	 * 比较ip地址是否在某网段内
	 * @param ip
	 * @param startIp
	 * @param endIp
	 * @return
	 */
	public static boolean isIncludeIp(String ip, String startIp, String endIp){
		long longIp = IpUtil.ip2long(ip);
		long longStartIp = IpUtil.ip2long(startIp);
		long longEndIp = IpUtil.ip2long(endIp);
		if(longIp >= longStartIp && longIp <= longEndIp){//指定ip在某ip段范围内
			return true;
		}
		return false;
	}

	/**
	 * 将IP地址转换成十进制整数
	 * @param ip 格式如：127.0.0.1
	 * @return
	 */
    public static long ip2long(String ip){
        long[] ipArray = new long[4];
        String[] ipSplits = ip.split("\\.");
        for(int i=0; i<ipSplits.length; i++){
			ipArray[i] = Long.parseLong(ipSplits[i]);
        }
        return (ipArray[0] << 24) + (ipArray[1] << 16) + (ipArray[2] << 8) + ipArray[3];
    }

	public static boolean isContainSubMask(String str){
		boolean flag = false;
		String ip = "";
		int mask = 0;
		if(str.contains("/")){
			int index = str.indexOf("/");
			String s1 =str.substring(0, index);
			String s2 = str.substring(index+1);
			if(verifyIp(s1)){
				ip = s1;
			}
			if(CommonUtil.isNum(s2)){
				mask = Integer.parseInt(s2);
			}
			if(!ip.equals("") && mask != 0){
				flag = true;
			}
		}
		return flag;
		
	}

	public static List<String> getIpWithSubnetMask(String ipAndSub){
		List<String> ips = new ArrayList<>();
		String ip = "";
		int mask = 0;
		if(ipAndSub.contains("/")){
			int index = ipAndSub.indexOf("/");
			ip =ipAndSub.substring(0, index);
			mask = Integer.parseInt(ipAndSub.substring(index+1, ipAndSub.length()));
		}
		long startIpLong = ip2long(ip);
		long endIpLong = (long) (startIpLong + Math.pow(2, mask));
		String startIp = longToIp(startIpLong);
		String endIp = longToIp(endIpLong);
		ips.add(startIp);
		ips.add(endIp);
		return ips;
	}
	public static String longToIp(Long ipL){
		StringBuilder sb = new StringBuilder();
		sb.append((ipL >> 24) & 0xFF).append(".");
		sb.append((ipL >> 16)& 0xFF).append(".");
		sb.append((ipL >> 8)& 0xFF).append(".");
		sb.append(ipL & 0xFF);
		return sb.toString(); 
	}
}