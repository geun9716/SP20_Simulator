package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
	
    ResourceManager rMgr;

    /* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Register 조작의 가독성을 위한 선언*/
	public static final int A = 0;
	public static final int X = 1;
	public static final int L = 2;
	public static final int B = 3;
	public static final int S = 4;
	public static final int T = 5;
	public static final int F = 6;
	public static final int PC = 8;
	public static final int SW = 9;
	
	int currentsection;
	HashMap<Integer, Inst> instMap;
	
    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
        instMap = new HashMap<Integer, Inst>();
		openFile("./src/SP20_simulator/inst.data");			//instMap을 위한 초기화
		currentsection = 0;
    }
    /**
     * fileName에서 가져온 값을 instMap에 저장한다.
     * @param fileName
     */
    public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader buffReader = new BufferedReader(filereader);
			String line = "";
			while((line = buffReader.readLine()) != null)
			{
				String [] inst = line.split(" ");
				Inst temp = new Inst(line);
				instMap.put(Integer.parseInt(inst[2],16), temp);
			}
			buffReader.close();
		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
    /**
     * instMap에서 data opcode 값을 찾아줌
     * @param data
     * @return opcode에 대한 형식 값
     */
    public int search_format(int data)
    {
    	data >>>= 4;					//Delete 4bit
    	data &= 252;					//Make zero nFlag & iFlag
    	if (instMap.containsKey(data))
    		return instMap.get(data).format;
    	else
    		return -1;
    }
    /**
     * data에 대한 flags 값을 찾아줌.
     * @return data에 대해 해당하는 flag값
     */
    public int getFlag(int data, int flags) {
		return data & flags;
	}
    
    // instruction 별로 동작을 수행하는 메소드를 정의
    /**
     * STL의 대한 명령을 실행하는 메소드
     * @param inst
     */
    public void STL(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
    		case 0:
    			if(inst.getFlag(eFlag)>0)	//extend 
    			{	
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
    			}
    			else						//immediate
    			{
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			}
    			break;
    		case bFlag: 					//basement
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    		case pFlag:						//Program Counter
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
				break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) 	//extend
    			{
    				rMgr.setMemory(rMgr.TA*2, String.format("%06X", rMgr.getRegister(L)).toCharArray(),6);
    			}
    			else						//normal type
    				rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(L)),6);
    			break;
    		case nFlag:	//indirect addressing
    			rMgr.setMemory(rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)), rMgr.intToChar(rMgr.getRegister(L)), 6);
    			break;
    		case iFlag:	//immediate addressing
    			rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(L)), 6);
    			break;
    	}
    }
    /**
     * JSUB에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void JSUB(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {		//extend 
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {							//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 						//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(3)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:							//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
		switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) {		//extend
    				rMgr.setRegister(L, rMgr.getRegister(PC));
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			else							//normal
    			{
    				rMgr.setRegister(L, rMgr.getRegister(PC));
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			break;
    		case nFlag:	//indirect addressing
    			break;
    		case iFlag:	//immediate addressing
    			break;
    	}
	}
    /**
     * LDA에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void LDA(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
    		case 0:
    			if(inst.getFlag(eFlag)>0) {		//extend 
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
    			}
    			else {							//immediate
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			}
    			break;
    		case bFlag:  						//basement
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    		case pFlag:							//Program Counter
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
		case nFlag|iFlag:	//simple addressing
			if(inst.getFlag(eFlag)>0) {			//extend	
				
			}
			else								//normal
				rMgr.setRegister(A, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)));
			break;
		case nFlag:	//indirect addressing
			break;
		case iFlag:	//immediate addressing
			rMgr.setRegister(A, rMgr.TA);
			break;
    	}
    }
    /**
     * CLEAR에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void CLEAR(Instruction inst) {
    	rMgr.setRegister(PC, rMgr.getRegister(PC)+2);
    	rMgr.setRegister((int)inst.ObjectCode.charAt(2)-'0', 0);
    	rMgr.TA = 0;
	}
    /**
     * LDT에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void LDT(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {		//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {							//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag:   						//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:							//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
			case nFlag|iFlag:	//simple addressing
				if(inst.getFlag(eFlag)>0) {			//extend	
					rMgr.setRegister(T, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)));
				}
				else								//normal
					rMgr.setRegister(T, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)));
				break;
			case nFlag:	//indirect addressing
				break;
			case iFlag:	//immediate addressing
				break;
    	}
    }
    /**
     * TD에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void TD(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
		case nFlag|iFlag:	//simple addressing
			if(inst.getFlag(eFlag)>0) {			//extend	
			}
			else								//normal
			{
				rMgr.testDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)));
			}
			break;
		case nFlag:	//indirect addressing
			break;
		case iFlag:	//immediate addressing
			break;
    	}
    }
    /**
     * JEQ에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void JEQ(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	if(rMgr.getRegister(SW) == 0)				//CC ==
    	{
    		switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) {			//extend
    				
    			}
    			else								//normal
    			{
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			break;
    		case nFlag:	//indirect addressing
    			break;
    		case iFlag:	//immediate addressing
    			break;
        	}
    	}
    }
    /**
     * RD에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void RD(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
			case nFlag|iFlag:	//simple addressing
				if(inst.getFlag(eFlag)>0) {			//extend
					rMgr.setRegister(A, (rMgr.readDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)), 1)));
				}
				else								//normal
				{
					rMgr.setRegister(A, (rMgr.readDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)), 1)));
				}
				break;
			case nFlag:	//indirect addressing
				rMgr.setRegister(A, rMgr.readDevice(new String(rMgr.getMemory(rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)),2)), 1));
				break;
			case iFlag:	//immediate addressing
				rMgr.setRegister(A, (rMgr.readDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)), 1)));
				break;
	    }
    }
    /**
     * COMPR에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void COMPR(Instruction inst) {
    	rMgr.setRegister(PC, rMgr.getRegister(PC)+2);
    	if(rMgr.getRegister(inst.ObjectCode.charAt(2)-'0') == rMgr.getRegister(inst.ObjectCode.charAt(3)-'0'))	//CC == : EQ
    		rMgr.setRegister(SW, 0);
    	else if (rMgr.getRegister(inst.ObjectCode.charAt(2)-'0') < rMgr.getRegister(inst.ObjectCode.charAt(3)-'0'))	//CC < : LT
    		rMgr.setRegister(SW, 1);
    	else															//else
    		rMgr.setRegister(SW, -1);
    	rMgr.TA = 0;
    }
    /**
     * STCH에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void STCH(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    	}
    	if(inst.getFlag(xFlag) > 0) {							//check xFlag
    		rMgr.TA += rMgr.getRegister(X);
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
		case nFlag|iFlag:						//simple addressing
			if(inst.getFlag(eFlag)>0) {			//extend
				rMgr.setMemory(rMgr.TA*2, new String(rMgr.intToChar(rMgr.getRegister(A))).substring(4, 6).toCharArray(), 2);
			}
			else								//normal
			{
				rMgr.setMemory(rMgr.TA*2, new String(rMgr.intToChar(rMgr.getRegister(A))).substring(4, 6).toCharArray(), 2);
			}
			break;
		case nFlag:								//indirect addressing
			rMgr.setMemory(rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)), new String(rMgr.intToChar(rMgr.getRegister(A))).substring(4, 6).toCharArray(), 2);
			break;
		case iFlag:								//immediate addressing
			rMgr.setMemory(rMgr.TA*2, new String(rMgr.intToChar(rMgr.getRegister(A))).substring(4, 6).toCharArray(), 2);
			break;
    	}
    }
    /**
     * TIXR에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void TIXR(Instruction inst) {
    	rMgr.setRegister(PC, rMgr.getRegister(PC)+2);
		rMgr.setRegister(X, rMgr.getRegister(X)+1);											// x = x+1
    	if (rMgr.getRegister(inst.ObjectCode.charAt(2)-'0') == rMgr.getRegister(X))			// CC == : EQ
    	{
    		rMgr.setRegister(SW, 0);
    	}
    	else if (rMgr.getRegister(inst.ObjectCode.charAt(2)-'0') > rMgr.getRegister(X))		// CC < : LT
    	{
    		rMgr.setRegister(SW, 1);
    	}
    	else																				//else
    	{
    		rMgr.setRegister(SW, -1);
    	}
    	rMgr.TA = 0;
    }
    /**
     * JLT에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void JLT(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
    	
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	if(rMgr.getRegister(SW) == 1)										//CC <
    	{
    		switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) {			//extend
    			}
    			else								//normal
    			{
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			break;
    		case nFlag:	//indirect addressing
    			break;
    		case iFlag:	//immediate addressing
    			break;
        	}
    	}
    }
    /**
     * STX에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void STX(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
			case 0:
				if(inst.getFlag(eFlag)>0)	//extend 
				{	
					rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
					rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
				}
				else						//immediate
				{
					rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
					rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
				}	
				break;
			case bFlag: 					//basement
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
				break;
			case pFlag:						//Program Counter
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
				break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) 	//extend
    			{
    				rMgr.setMemory(rMgr.TA*2, String.format("%06X", rMgr.getRegister(X)).toCharArray(),6);
    			}
    			else						//normal type
    				rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(X)),6);
    			break;
    		case nFlag:	//indirect addressing
    			rMgr.setMemory(rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)), rMgr.intToChar(rMgr.getRegister(X)), 6);
    			break;
    		case iFlag:	//immediate addressing
    			rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(X)), 6);
    			break;
		}
    }
    /**
     * RSUB에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void RSUB(Instruction inst) {
    	rMgr.device = " ";												//device 초기화
    	SicSimulator.locate = rMgr.getRegister(L)*2;					//L레지스터 값을 locate로 설정
    	rMgr.setRegister(PC, rMgr.getRegister(L));						//PC값 설정
    }
    /**
     * COMP에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void COMP(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0)	//extend 
			{	
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else						//immediate
			{
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}	
			break;
		case bFlag: 					//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:						//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
		case nFlag|iFlag:	//simple addressing
			if(inst.getFlag(eFlag)>0) 	//extend
			{
				if (rMgr.getRegister(A) == rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
					rMgr.setRegister(SW, 0);
				else if (rMgr.getRegister(A) < rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
					rMgr.setRegister(SW, 1);
				else
					rMgr.setRegister(SW, -1);
			}
			else						//normal type
				if (rMgr.getRegister(A) == rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
					rMgr.setRegister(SW, 0);
				else if (rMgr.getRegister(A) < rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
					rMgr.setRegister(SW, 1);
				else
					rMgr.setRegister(SW, -1);
				break;
		case nFlag:	//indirect addressing
			if (rMgr.getRegister(A) == rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
				rMgr.setRegister(SW, 0);
			else if (rMgr.getRegister(A) < rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)))
				rMgr.setRegister(SW, 1);
			else
				rMgr.setRegister(SW, -1);
			break;
		case iFlag:	//immediate addressing
			if (rMgr.getRegister(A) == rMgr.TA)
				rMgr.setRegister(SW, 0);
			else if (rMgr.getRegister(A) < rMgr.TA)
				rMgr.setRegister(SW, 1);
			else
				rMgr.setRegister(SW, -1);
			break;
    	}
    }
    /**
     * LDCH에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void LDCH(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
    		case 0:
    			if(inst.getFlag(eFlag)>0) {		//extend 
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
    			}
    			else {							//immediate
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			}
    			break;
    		case bFlag:  						//basement
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    		case pFlag:							//Program Counter
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    	}
    	if(inst.getFlag(xFlag) > 0) {							//check xFlag
    		rMgr.TA += rMgr.getRegister(X);
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
		case nFlag|iFlag:	//simple addressing
			if(inst.getFlag(eFlag)>0) {			//extend	
				if (rMgr.getMemory(rMgr.TA*2, 2)[0] != '\u0000')
				{
					rMgr.setRegister(A, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 2)));
				}
				else
					;
			}
			else								//normal
				rMgr.setRegister(A, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 2)));
			break;
		case nFlag:	//indirect addressing
			break;
		case iFlag:	//immediate addressing
			break;
    	}
    }
    /**
     * WD에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void WD(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
			case nFlag|iFlag:	//simple addressing
				if(inst.getFlag(eFlag)>0) {			//extend
					rMgr.writeDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)), (char)rMgr.getRegister(A), 1);
				}
				else								//normal
				{
					rMgr.writeDevice(new String(rMgr.getMemory(rMgr.TA*2, 2)), (char)rMgr.getRegister(A), 1);
				}
				break;
			case nFlag:	//indirect addressing
				break;
			case iFlag:	//immediate addressing
				break;
	    }
    }
    /**
     * J에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void J(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
		case 0:
			if(inst.getFlag(eFlag)>0) {				//extend
				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
			}
			else {									//immediate
				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			}
			break;
		case bFlag: 								//basement
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
		case pFlag:									//Program Counter
			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
			break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) {			//extend
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			else								//normal
    			{
    				SicSimulator.locate = rMgr.TA*2;
    				rMgr.setRegister(PC, rMgr.TA);
    			}
    			break;
    		case nFlag:	//indirect addressing
    			SicSimulator.locate = rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6));
				rMgr.setRegister(PC, rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)));
    			break;
    		case iFlag:	//immediate addressing
    			SicSimulator.locate = rMgr.TA*2;
				rMgr.setRegister(PC, rMgr.TA);
    			break;
        }
    }
    /**
     * STA에 대한 명령어를 실행하는 메소드
     * @param inst
     */
    public void STA(Instruction inst) {
    	switch(inst.getFlag(bFlag|pFlag)) {
    		case 0:
    			if(inst.getFlag(eFlag)>0)	//extend 
    			{	
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+4);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 8).toCharArray());
    			}
    			else						//immediate
    			{
    				rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    				rMgr.TA = rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			}
    			break;
    		case bFlag: 					//basement
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(B)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
    			break;
    		case pFlag:						//Program Counter
    			rMgr.setRegister(PC, rMgr.getRegister(PC)+3);
    			rMgr.TA = rMgr.getRegister(PC)+rMgr.charToInt(inst.ObjectCode.substring(3, 6).toCharArray());
				break;
    	}
    	switch(inst.getFlag(nFlag|iFlag)) {
    		case nFlag|iFlag:	//simple addressing
    			if(inst.getFlag(eFlag)>0) 	//extend
    			{
    				rMgr.setMemory(rMgr.TA*2, String.format("%06X", rMgr.getRegister(A)).toCharArray(),6);
    			}
    			else						//normal type
    				rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(A)),6);
    			break;
    		case nFlag:	//indirect addressing
    			rMgr.setMemory(rMgr.charToInt(rMgr.getMemory(rMgr.TA*2, 6)), rMgr.intToChar(rMgr.getRegister(A)), 6);
    			break;
    		case iFlag:	//immediate addressing
    			rMgr.setMemory(rMgr.TA*2, rMgr.intToChar(rMgr.getRegister(A)), 6);
    			break;
    	}
    }
}
/* 
 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
 *  
 * ex)
 * String instruction;
 * int opcode;
 * int format;
 */
class Inst {
	String instruction;
	int format;
	int opcode;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Inst(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		String[] info = line.split(" ");
		instruction = info[0];
		format = Integer.parseInt(info[1]);
		opcode = Integer.parseInt(info[2], 16);
	}
}