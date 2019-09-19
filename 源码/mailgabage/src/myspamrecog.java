import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class myspamrecog {
	@SuppressWarnings("null")
	public static void main(String args[]){
		String allword[];
		List<String> allmail = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("TrainingSet/SMSSpamCollection"));
			String str;
			String allstr="";
			int mailnum=0;
			int spamnum=0;
			int legitnum=0;
			Map<String, keyword> keyMap = new HashMap<String, keyword>();
			try {
				while ((str = br.readLine()) != null)
				{
					allmail.add(str.trim());
					mailnum++;
					allstr+=str.replaceAll("spam", "").replaceAll("ham", "").trim().toString();
				}
				br.close();
				allword=allstr.split("[^a-zA-Z]+");
				for (int i=0;i<allword.length;i++)
				{
					String s=allword[i];
					keyMap.put(s, new keyword(s, 0, 0));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=0;i<mailnum;i++){
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
			BufferedReader testbr = new BufferedReader(new FileReader("TestSet/TestFile.txt"));
			try {
				while ((str = testbr.readLine()) != null)
				{
					testmail.add(str.trim());
					testmailnum++;
				}
				testbr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int rightnum=0;
			int errornum=0;
			int spamcount = 0;
			double Pup;
			double Pdown;
			for(int i=0;i<testmailnum;i++){
				if(testmail.get(i).startsWith("spam")){
					spamcount++;
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
					System.out.println("该封邮件是垃圾邮件的概率为:" + Pmail + ",实际是否为垃圾邮件:" + testmail.get(i).startsWith("spam"));
					// 成功识别
					if (Pmail > 0.999 && testmail.get(i).startsWith("spam"))
					{
						rightnum++;
					}
					// 识别错误
					if (Pmail > 0.999 && testmail.get(i).startsWith("ham"))
					{
						errornum++;
					}
				}
			}
			System.out.println("垃圾邮件总数" + spamcount + ",正确识别了" + rightnum + "封垃圾邮件，召回率" + rightnum * 1.0 / spamcount + ",准确率：" + rightnum * 1.0
					/ (rightnum + errornum));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class keyword
{
	public String keyword;
	public int inspam;
	public int inlegit;
	public double isspamprob;

	public keyword(String keyword, int inspam, int inlegit)
	{
		this.keyword = keyword;
		this.inspam = inspam;
		this.inlegit = inlegit;
	}
}