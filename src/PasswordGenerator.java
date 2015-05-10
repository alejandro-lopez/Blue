import java.util.Random;


public class PasswordGenerator {

	public PasswordGenerator() {
		// TODO Auto-generated constructor stub
	}
	String generate(int val1,int val2,int val3) {
		char[] values1 = {'w','e','l','c','s','p','a','m','S','X','M','A','H','E','V','x'};
	    char[] values2 = {'Z','W','B','T','Y','P','I','L'};
	    char[] values3 = {'1','2','3','4','5','6','7','8','9','0'};
	    String out1="";
	    String out2="";
	    String out3="";
	    Random rand = new Random();
         for (int i=0;i<val1;i++) {
            int idx=rand.nextInt(values1.length);
            out1+= values1[idx];
         }
 
         for (int i=0;i<val3;i++) {
            int idx=rand.nextInt(values3.length);
             out2+= values3[idx];
         }
 
         for (int i=0;i<val2;i++) {
            int idx=rand.nextInt(values2.length);
             out3+= values2[idx];
         }
	 
	    String out= out1.concat(out3).concat(out2);
	    return out;
	}
}
