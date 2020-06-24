package SP20_simulator;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;



/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; // String���� �����ؼ� ����Ͽ��� ������.
	int[] register = new int[10];
	double register_F;
	SymbolTable symtabList;
	
	//���α׷� �̸�, �����ּ�, ����, EndRecord�� ���� ����
	ArrayList <String> ProName;
	ArrayList <Integer> StartAddr;
	ArrayList <Integer> Length;
	String PRONAME;
	int STARTADDR;	
	int ProLength;
	ArrayList <Integer> EndRecord;
	//���α׷��� Visual���� ����� �����ֱ� ���� ����
	int TA;
	Vector <String> Instruction;
	ArrayList <String> Log;
	String device;
	//���α׷��� ���� Ȯ���ϴ� ����
	boolean isFinish;
	ResourceManager(){
		initializeResource();
	}
	
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	
	public void initializeResource(){
		symtabList = new SymbolTable();
		ProName = new ArrayList <String> ();
		StartAddr = new ArrayList <Integer> ();
		Length = new ArrayList <Integer> ();
		EndRecord = new ArrayList <Integer> ();
		ProLength = 0;
		Instruction = new Vector <String> ();
		Log = new ArrayList <String>();
		device = " ";
		isFinish = false;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 */
	public void closeDevice() {
		deviceManager.clear();
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		
		if (!deviceManager.containsKey(devName))
		{
			try {
				RandomAccessFile raf = new RandomAccessFile(devName+".txt", "rw");
				deviceManager.put(devName, raf);
			} catch (Exception e) {
				setRegister(9, 0);
				e.printStackTrace();
			}
		}
		device = devName;
		setRegister(9, -1);
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 */
	public char readDevice(String devName, int num){
		int data = 0;
		try {
			RandomAccessFile raf = (RandomAccessFile) deviceManager.get(devName);
			data = raf.read();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (data < 0)
				data = 0;
		}		
		return (char)data;
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, char data, int num){
		try {
			RandomAccessFile raf = (RandomAccessFile) deviceManager.get(devName);
			raf.write((int)data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		char[] temp = new char[num];
		for(int i = 0 ; i < num ; i++)
			temp[i] = memory[location+i];
		return temp;
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){
		for(int i = 0; i < num ; i++)
			memory[locate+i] = data[i];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return this.register[regNum];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		this.register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		return String.format("%06X", data).toCharArray();
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int charToInt(char[] data){
		int result = 0;
		if (data[0] == 'F')
			result = Integer.parseInt(new String(data), 16) - 0x1000;
		else
			result = Integer.parseInt(new String(data), 16);
		return result;
	}
	/**
	 * ���α׷��� �̸��� �������ִ� �޼ҵ�
	 * @param str
	 */
	public void setProName(String str) {
		this.ProName.add(str);
	}
	/**
	 * ���α׷��� �����ּҸ� �������ִ� �޼ҵ�
	 * @param addr
	 */
	public void setStartAddr (int addr) {
		this.StartAddr.add(addr);
	}
	/**
	 * ���α׷��� ��ü ���̿� ���� ������ ���̸� �������ִ� �޼ҵ�
	 * @param len
	 */
	public void setProLength(int len) {
		this.ProLength += len;
		this.Length.add(len*2);
	}
	
	/**
	 * ���޹��� section�� �����ּҸ� ��ȯ���ִ� �޼ҵ�
	 * @param section
	 * @return �ش� section�� �����ּ�
	 */
	public int getStartAddr(int section) {
		return this.StartAddr.get(section);
	}
}