package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	int currentsection = 0;
	ArrayList <String> MRecord;
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
		MRecord = new ArrayList <String>();
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		int locate = 0;
		int num = 0;
		//pass 1
		try {
			String line = "";
			FileReader filereader = new FileReader(objectCode);
			BufferedReader buffReader = new BufferedReader(filereader);
			while((line = buffReader.readLine()) != null)
			{
				switch(line.charAt(0)) {
					case 'H':										//H Record
						String[] arr = line.split("\t");
						rMgr.setProName(arr[0].substring(1));
						rMgr.setStartAddr(Integer.parseInt(arr[1].substring(0, 6),16)+rMgr.ProLength*2);
						rMgr.setProLength(Integer.parseInt(arr[1].substring(6),16));
						rMgr.symtabList.putSymbol(arr[0].substring(1), rMgr.getStartAddr(currentsection));		//put symtab
						break;
					case 'D':										//D Record
						line = line.substring(1);
						String [] arr1 = line.split("\\D+");					
						String [] symbol = new String[arr1.length];
						String [] addr = new String[arr1.length];
						for(int i = 1 ; i <= arr1.length-1; i++)										//put Symtab
						{
							symbol[i] = line.substring(line.indexOf(arr1[i])-6, line.indexOf(arr1[i]));
							addr[i] = line.substring(line.indexOf(arr1[i]), line.indexOf(arr1[i])+6);
							rMgr.symtabList.putSymbol(symbol[i], Integer.parseInt(addr[i], 16)*2);
						}
						break;
					case 'T':										//T Record
						locate = Integer.parseInt(line.substring(1, 7), 16)*2 + rMgr.getStartAddr(currentsection);	
						num = Integer.parseInt(line.substring(7, 9), 16)*2;
						rMgr.setMemory(locate, line.substring(9).toCharArray(), num);
						break;
					case 'M':										//M Record
						int loctmp = (Integer.parseInt(line.substring(1, 7), 16)*2 + rMgr.getStartAddr(currentsection))/2;
						line = line.replace(line.substring(1, 7), String.format("%06X", loctmp));
						MRecord.add(line.substring(1));
						break;
					case 'E':										//End Record
						rMgr.EndRecord.add(rMgr.getStartAddr(currentsection));
						currentsection++;
						break;
				}
			}
			buffReader.close();
		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			System.out.println(e);
		}
		
		//Pass 2
		for(int i = 0 ; i < MRecord.size(); i++)
		{
			int addr;
			int result = 0;

			if(MRecord.get(i).charAt(8) == '+')															// + Step
			{
				String [] tmp = MRecord.get(i).split("\\+");
				locate = Integer.parseInt(tmp[0].substring(0,6),16)*2;
				addr = rMgr.symtabList.search(tmp[1]);
				if(MRecord.get(i).substring(6, 8).equals("05"))											//extend Modify
				{
					result = Integer.parseInt(new String(rMgr.getMemory(locate+2, 4)),16) + addr;
					rMgr.setMemory(locate+2, String.format("%04X", result/2).toCharArray(), 4);
				}
				else if (MRecord.get(i).substring(6, 8).equals("06"))									//word Modify
				{
					result = Integer.parseInt(new String(rMgr.getMemory(locate, 6)),16)*2 + addr;
					rMgr.setMemory(locate, String.format("%06X", result/2).toCharArray(), 6);
				}
			}
			else if (MRecord.get(i).charAt(8) == '-')													// - Step
			{
				String [] tmp = MRecord.get(i).split("\\-");
				locate = Integer.parseInt(tmp[0].substring(0,6),16)*2;
				addr = rMgr.symtabList.search(tmp[1]);
				if(MRecord.get(i).substring(6, 8).equals("05"))											//extend Modify
				{
					result = Integer.parseInt(new String(rMgr.getMemory(locate+2, 4)),16) - addr;
					rMgr.setMemory(locate+2, String.format("%04X", result/2).toCharArray(), 4);
				}
				else if (MRecord.get(i).substring(6, 8).equals("06"))									//word Modify
				{
					result = Integer.parseInt(new String(rMgr.getMemory(locate, 6)),16)*2 - addr;
					rMgr.setMemory(locate, String.format("%06X", result/2).toCharArray(), 6);
				}
			}
		}
	}
}
