package net.htjs.mobile.nyoa.db;


public final class UserInfo {
	int _id;
	String _name;
	String _phone_number;
	String _password;

	public UserInfo() {

	}

	public UserInfo(int id, String name, String _phone_number, String password) {
		this._id = id;
		this._name = name;
		this._phone_number = _phone_number;
		this._password = password;
	}

	public UserInfo(int id, String name, String _phone_number) {
		this._id = id;
		this._name = name;
		this._phone_number = _phone_number;
	}

	public UserInfo(String name, String _phone_number) {
		this._name = name;
		this._phone_number = _phone_number;
	}

	public int getID() {
		return this._id;
	}

	public UserInfo setID(int id) {
		this._id = id;
		return this;
	}

	public String getName() {
		return this._name;
	}

	public UserInfo setName(String name) {
		this._name = name;
		return this;
	}

	public String getPhoneNumber() {
		return this._phone_number;
	}

	public UserInfo setPhoneNumber(String phone_number) {
		this._phone_number = phone_number;
		return this;
	}

	public String getPassword() {
		return this._password;
	}

	public UserInfo setPassword(String password) {
		this._password = password;
		return this;
	}

	public String toString() {
		return "UserInfo：" + " id:" + _id + " name:" + _name + " password:"
				+ _password;
	}
	public static void main(String[] args){
		System.out.println("www");//new UserInfo().download("D:/12.pdf","http://16.64.1.1/uploadfile/2013/0305/20130305103542345.pdf");
	}
	/*HttpClient client = new HttpClient();
	public  String download(String path, String url) {
		File f = new File(path);
		File dir = new File(f.getAbsolutePath().substring(0,
				f.getAbsolutePath().lastIndexOf(File.separator)));
		try {
			if (!dir.exists())
				dir.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GetMethod get = new GetMethod(url);
		try {
			client.executeMethod(get);
			String name = new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date());
			FileOutputStream fileOutputStream = new FileOutputStream(new File(path + name + ".jpg"));
			fileOutputStream.write(get.getResponseBody());
			fileOutputStream.close();
			 
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			 
		} 
		return null;
	}*/
}
