
public class substest {
	public static void main(String[] args) {
		
		String a = "윤형식신재원";
		
		System.out.println(a.substring(3));
		System.out.println(a.substring(3, 6));
		
		String c = a.substring(3,3);
		if (c.equals(""))
		{
			System.out.println("empty");
		}	else {
			System.out.println("dd");
		}

		String b = "/user/create?userId=javajigi";
		
		
		String indextest = "/user/create?";
		
		System.out.println(indextest.indexOf("?"));
		
		System.out.println(indextest.indexOf("가"));
	}
}
