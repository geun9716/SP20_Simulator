package SP20_simulator;

import java.io.File;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	InstLuncher IL;					//��ɾ� ��ó
	public static int locate = 0;	//locate position ����
	int currentsection = 0;			//current section ����
	
	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
		IL = new InstLuncher(resourceManager);
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
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
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() {
		//instruction�� opcode ������ �ľ��ϰ�, �޸𸮿��� data�� ���� ���� �°� �������� �ܰ�
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
		
		// currentsection�� ���ϴ� �ܰ�
		for(int i = 0 ; i < rMgr.StartAddr.size(); i++)
		{
			if (locate > rMgr.getStartAddr(i))
			{
				currentsection = i;
			}
		}
		
		//Instructions�� �ڵ带 �����ϰ� Instruction��ü�� �����ϴ� �ܰ�
		rMgr.Instruction.add(new String(data));
		Instruction instruction = new Instruction(new String(data));
		
		//������ Instruction��ü�� opcode�� ���Ͽ� ��ɾ� ��ó�� �ִ� �ڵ带 �������� �ܰ�
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
		//���α׷��� �������� Ȯ���ϴ� �ܰ�
		if(isDone())
			return ;
	}
	
	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
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
	 * ���α׷��� �������� Ȯ���ϴ� �޼ҵ�. PC���� ���α׷� �����ּҰ� ������ ���Ѵ�.
	 * @return �������� true, ������ �ʾҴٸ� false
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
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
		rMgr.Log.add(log);
	}	
}
/**
 * Instruction�� ���� ������ �����ϴ� Ŭ����
 * ObjectCode, nixbpe, opcode ���� �����Ѵ�.
 */
class Instruction {
	String ObjectCode;
	int nixbpe;
	int opcode;
	
	Instruction(String obj){
		this.ObjectCode = obj;
		parsing(obj);
	}
	//String���� �Ѿ�� obj�� �Ľ��ϴ� �޼ҵ�
	void parsing(String obj) {
		int temp = Integer.parseInt(obj.substring(0, 3), 16);
		nixbpe = temp & 63;
		opcode = Integer.parseInt(obj.substring(0, 2), 16) & 252;
	}
	//Instruction�� nixbpe�� flags�� ���� ���� �������� �޼ҵ�
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
