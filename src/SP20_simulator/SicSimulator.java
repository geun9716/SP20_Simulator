package SP20_simulator;

import java.io.File;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	InstLuncher IL;					//명령어 런처
	public static int locate = 0;	//locate position 변수
	int currentsection = 0;			//current section 변수
	
	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
		IL = new InstLuncher(resourceManager);
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등*/
		rMgr.setRegister(0, 0);
		rMgr.setRegister(1, 0);
		rMgr.setRegister(2, 0);
		rMgr.setRegister(3, 0);
		rMgr.setRegister(4, 0);
		rMgr.setRegister(5, 0);
		rMgr.register_F = 0;
		rMgr.setRegister(8, 0);
		rMgr.setRegister(9, 0);

		locate = rMgr.getStartAddr(currentsection);
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		//instruction의 opcode 형식을 파악하고, 메모리에서 data에 형식 수의 맞게 가져오는 단계
		char [] opcode = rMgr.getMemory(locate, 3);
		char [] data = {};
		int op = rMgr.charToInt(opcode);
		
		if (IL.search_format(op) == 2)		//format 2
		{
			data = rMgr.getMemory(locate, 4);
			locate += 4;
		}
		else if (IL.getFlag(op, InstLuncher.eFlag) == 1)		//extend type
		{
			data = rMgr.getMemory(locate, 8);
			locate += 8;
		}
		else if (IL.search_format(op) == 3)		//format 3
		{
			data = rMgr.getMemory(locate, 6);
			locate += 6;
		}
		
		// currentsection을 구하는 단계
		for(int i = 0 ; i < rMgr.StartAddr.size(); i++)
		{
			if (locate > rMgr.getStartAddr(i))
			{
				currentsection = i;
			}
		}
		
		//Instructions에 코드를 저장하고 Instruction객체를 생성하는 단계
		rMgr.Instruction.add(new String(data));
		Instruction instruction = new Instruction(new String(data));
		
		//생성한 Instruction객체의 opcode를 비교하여 명령어 런처에 있는 코드를 가져오는 단계
		if(instruction.opcode == 0x14)
		{
			IL.STL(instruction);
			addLog("STL");
		}
		else if (instruction.opcode == 0x48)
		{
			IL.JSUB(instruction);
			addLog("JSUB");
		}
		else if (instruction.opcode == 0x0)
		{
			IL.LDA(instruction);
			addLog("LDA");
		}
		else if (instruction.opcode == 0x28)
		{
			IL.COMP(instruction);
			addLog("COMP");
		}
		else if (instruction.opcode == 0x30)
		{
			IL.JEQ(instruction);
			addLog("JEQ");
		}
		else if (instruction.opcode == 0x3c)
		{
			IL.J(instruction);
			addLog("J");
		}
		else if (instruction.opcode == 0x0c)
		{
			IL.STA(instruction);
			addLog("STA");
		}
		else if (instruction.opcode == 0xB4)
		{
			IL.CLEAR(instruction);
			addLog("CLEAR");
		}
		else if (instruction.opcode == 0x74)
		{
			IL.LDT(instruction);
			addLog("LDT");
		}
		else if (instruction.opcode == 0xE0)
		{
			IL.TD(instruction);
			addLog("TD");
		}
		else if (instruction.opcode == 0xD8)
		{
			IL.RD(instruction);
			addLog("RD");
		}
		else if (instruction.opcode == 0xA0)
		{
			IL.COMPR(instruction);
			addLog("COMPR");
		}
		else if (instruction.opcode == 0x54)
		{
			IL.STCH(instruction);
			addLog("STCH");
		}
		else if (instruction.opcode == 0xB8)
		{
			IL.TIXR(instruction);
			addLog("TIXR");
		}
		else if (instruction.opcode == 0x38)
		{
			IL.JLT(instruction);
			addLog("JLT");
		}
		else if (instruction.opcode == 0x10)
		{
			IL.STX(instruction);
			addLog("STX");
		}
		else if (instruction.opcode == 0x4C)
		{
			IL.RSUB(instruction);
			addLog("RSUB");
		}
		else if (instruction.opcode == 0x50)
		{
			IL.LDCH(instruction);
			addLog("LDCH");
		}
		else if (instruction.opcode == 0xDC)
		{
			IL.WD(instruction);
			addLog("WD");
		}
		//프로그램이 끝났는지 확인하는 단계
		if(isDone())
			return ;
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		while(true)
		{
			if(!rMgr.isFinish)
				oneStep();
			else
				break;
		}
	}
	/**
	 * 프로그램이 끝났는지 확인하는 메소드. PC값과 프로그램 시작주소가 같은지 비교한다.
	 * @return 끝났으면 true, 끝나지 않았다면 false
	 */
	public boolean isDone() {
		if(rMgr.getRegister(8) == rMgr.getStartAddr(0))
		{
			rMgr.isFinish = true;
			return true;
		}
		else
			return false;
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		rMgr.Log.add(log);
	}	
}
/**
 * Instruction에 대한 정보를 저장하는 클래스
 * ObjectCode, nixbpe, opcode 값을 저장한다.
 */
class Instruction {
	String ObjectCode;
	int nixbpe;
	int opcode;
	
	Instruction(String obj){
		this.ObjectCode = obj;
		parsing(obj);
	}
	//String으로 넘어온 obj를 파싱하는 메소드
	void parsing(String obj) {
		int temp = Integer.parseInt(obj.substring(0, 3), 16);
		nixbpe = temp & 63;
		opcode = Integer.parseInt(obj.substring(0, 2), 16) & 252;
	}
	//Instruction에 nixbpe에 flags에 대한 값을 가져오는 메소드
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
