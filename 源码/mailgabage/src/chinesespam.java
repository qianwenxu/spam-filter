import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;


public class chinesespam {
	@SuppressWarnings("null")
	public static void main(String args[]) throws FileNotFoundException{
		//String allspamword[];
		List<String> allmail = new ArrayList<String>();
		String str;
		String allstr="";
		int mailnum=0;
		int spamnum=0;
		int legitnum=0;
		Map<String, keyword> keyMap = new HashMap<String, keyword>();
		File file = new File("ham_all");
		String[] filelist = file.list();
		for(int m=0;m<filelist.length;m++){
			String filename="ham_all\\"+filelist[m];
			//BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				allstr="";
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));
				while ((str = br.readLine()) != null)
				{
					allmail.add("ham "+str.trim());
					//System.out.println("ham:"+str.trim());
					mailnum++;
					allstr+=str.trim().toString();
				}
				br.close();
				Analyzer anal=new IKAnalyzer(true);		
				StringReader reader=new StringReader(allstr);
				TokenStream ts=anal.tokenStream("", reader);
				CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);
				//ArrayList<String> ans = new ArrayList<String>();
				Pattern pattern=Pattern.compile("[0-9]*$");
				while(ts.incrementToken()){
					String tmpstr=term.toString();
					Matcher isNum = pattern.matcher(tmpstr);
					if(!isNum.matches()&&!unusable(tmpstr)){
						keyMap.put(term.toString(), new keyword(tmpstr, 0, 0));
						//System.out.print(term.toString()+"|");
					}
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("ham_all结束");
		file = new File("spam_all");
		filelist = file.list();
		for(int m=0;m<filelist.length;m++){
			String filename="spam_all\\"+filelist[m];
			try {
				allstr="";
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));
				while ((str = br.readLine()) != null)
				{
					allmail.add("spam "+str.trim());
					mailnum++;
					allstr+=str.trim().toString();
					//System.out.println("spamfile"+filelist[m]);
				}
				br.close();
				Analyzer anal=new IKAnalyzer(true);		
				StringReader reader=new StringReader(allstr);
				TokenStream ts=anal.tokenStream("", reader);
				CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);
				//ArrayList<String> ans = new ArrayList<String>();
				while(ts.incrementToken()){
					keyMap.put(term.toString(), new keyword(term.toString(), 0, 0));
					//System.out.print(term.toString()+"|");
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("spam_all结束mailnum"+mailnum);
		for(int i=0;i<mailnum;i++){
			if(i%100==0){
				System.out.println("mail"+i);
			}
			if(allmail.get(i).startsWith("spam")){	
				for (Map.Entry<String, keyword> thekey : keyMap.entrySet())
				{
					keyword thekeyword = thekey.getValue();
					String keystr=thekey.getKey();
					if (allmail.get(i).toLowerCase().indexOf(keystr.toLowerCase()) >= 0)
					{
						thekeyword.inspam += 1;
					}
				}
				spamnum++;
			}else{
				for (Map.Entry<String, keyword> thekey : keyMap.entrySet())
				{
					keyword thekeyword = thekey.getValue();
					String keystr=thekey.getKey();
					if (allmail.get(i).toLowerCase().indexOf(keystr.toLowerCase()) >= 0)
					{
						thekeyword.inlegit += 1;
					}
				}
				legitnum++;
			}
		}
		List<String> remove = new ArrayList<String>();
		int removenum=0;
		for (Map.Entry<String, keyword> thekey : keyMap.entrySet())
		{
			keyword kw = thekey.getValue();
			if (kw.inspam + kw.inlegit == 0)
			{
				remove.add(thekey.getKey());
				removenum++;
			}else{
				double Spam = 1.0 * kw.inspam / spamnum;
				double Spamsum = 1.0 * spamnum / (spamnum + legitnum);
				double Legit = 1.0 * kw.inlegit / legitnum;
				double Legitsum = 1.0 * legitnum / (spamnum + legitnum);
				kw.isspamprob=(Spam * Spamsum) / (Spam * Spamsum + Legit * Legitsum);
				if (kw.isspamprob < 0.93)
				{
					remove.add(thekey.getKey());
					removenum++;
				}
			}
		}
		for(int i=0;i<removenum;i++){
			keyMap.remove(remove.get(i));
		}
		List<String> testmail = new ArrayList<String>();
		int testmailnum=0;
		System.out.println("下面测试");
		file = new File("test_all\\ham");
		filelist = file.list();
		for(int m=0;m<filelist.length;m++){
			String filename="test_all\\ham\\"+filelist[m];
			//BufferedReader testbr = new BufferedReader(new FileReader(filename));
			try {
				BufferedReader testbr = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));
				while ((str = testbr.readLine()) != null)
				{
					testmail.add("ham "+str.trim());
					testmailnum++;
				}
				testbr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		file = new File("test_all\\spam");
		filelist = file.list();
		for(int m=0;m<filelist.length;m++){
			String filename="test_all\\spam\\"+filelist[m];
			try {
				BufferedReader testbr = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf-8"));
				while ((str = testbr.readLine()) != null)
				{
					testmail.add("spam "+str.trim());
					testmailnum++;
					System.out.println("spamfile"+filelist[m]);
				}
				testbr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int rightnum=0;
		int errornum=0;
		int spamcount = 0;
		double Pup;
		double Pdown;
		for(int i=0;i<testmailnum;i++){
			if(testmail.get(i).startsWith("spam")){
				spamcount++;
				//System.out.println(testmail.get(i));
			}
			List<String> oneMailKeyword = new ArrayList<String>();

			for (Map.Entry<String, keyword> thekey : keyMap.entrySet())
			{
				if (testmail.get(i).toLowerCase().indexOf(thekey.getKey().toLowerCase()) >= 0)
				{
					oneMailKeyword.add(thekey.getKey());
				}
			}
			if (oneMailKeyword.size() > 0)
			{
				Pup = 1.0 * spamnum / (spamnum + legitnum);
				Pdown = 1.0f;
				for (String kw : oneMailKeyword)
				{
					Pup = Pup * keyMap.get(kw).inspam / spamnum;
					Pdown = Pdown * (keyMap.get(kw).inspam + keyMap.get(kw).inlegit) / (spamnum + legitnum);
				}
				double Pmail = Pup / (Pup + Pdown);
				System.out.println("该封邮件是垃圾邮件的概率为:" + Pmail + "Pup："+Pup+"Pdown:"+Pdown+",实际是否为垃圾邮件:" + testmail.get(i).startsWith("spam"));
				// 成功识别
				if (Pmail > 0.99999 && testmail.get(i).startsWith("spam"))
				{
					rightnum++;
				}
				// 识别错误
				if (Pmail > 0.99999 && testmail.get(i).startsWith("ham"))
				{
					errornum++;
					//System.out.print("error");
					//System.out.print("整段："+testmail.get(i));
					//for (String kw : oneMailKeyword)
					//{
						//System.out.print("单词："+kw+"，"+keyMap.get(kw).inspam / spamnum);
					//}
					//System.out.println();
				}
			}
		}
		System.out.println("垃圾邮件总数" + spamcount + ",正确识别了" + rightnum + "封垃圾邮件，召回率" + rightnum * 1.0 / spamcount + ",准确率：" + rightnum * 1.0
				/ (rightnum + errornum));
	}
	static boolean unusable(String str){
		if(str.equals("transfer")||str.equals("sp")||str.equals("yp")||str.equals("encoding")||str.equals("base64")||str.equals("mime-version")||str.equals("subject")
				||str.equals("mailer")||str.equals("charset")||str.equals("content-type")||str.equals("x-mailer")||str.equals("mime")||str.equals("content-transfer-encoding")
				||str.equals("type")||str.equals("plain")||str.equals("gb2312")||str.equals("version")){
			return true;
		}
		return false;
	}
}
//class keyword
//{
	//public String keyword;
	//public int inspam;
	//public int inlegit;
	//public double isspamprob;

	//public keyword(String keyword, int inspam, int inlegit)
	//{
		//this.keyword = keyword;
		//this.inspam = inspam;
		//this.inlegit = inlegit;
	//}
//}