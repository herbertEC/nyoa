package net.htjs.mobile.nyoa.db;

public final class ConfigInfo{
	public int id;
	public String isRecord;
	public String pattern;
	
	
	public ConfigInfo(int b,String a,String c){
		this.id=b;
		this.isRecord=a;
		this.pattern=c;
	}
	public String toString() {
		return "ConfigInfo：" + " id:" + id + " isRecord:" + isRecord + " pattern:"
				+ pattern;
	}
	
	
}