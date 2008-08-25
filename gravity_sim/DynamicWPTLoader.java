package gravity_sim;

import java.io.*;

public class DynamicWPTLoader {
	
	/* ------- Prefetch Standard! ------------------- */
	private static final int STANDARDBUFFERSIZE = 45000; 
	/* bounds are currentstep - buffersize until currentstep + buffersize */
	/* so there is a total current loaded stepcount of 2*buffersize+1     */
	/* ---------------------------------------------- */
	
	public static final int INFILE_NOERROR = -1;
	public static final int INFILE_FILENOTFOUND = -2;
	public static final int INFILE_READERROR = -3;
	public static final int INFILE_EOFSTARTERROR = -4;
	public static final int INFILE_EOFSTEPERROR = -5;
	public static final int INFILE_EOFOBJERROR = -6;	
	
	Step[] steps;
	File fpInputFile;
	int iBufferSize;
	int iCurMax;
	int iCurMin;
	int iLastStep;
	
	double dtimeCount;
	
	DynamicWPTLoader(File inputfile) {
		iBufferSize = STANDARDBUFFERSIZE;
		fpInputFile = inputfile;
	}
	
	DynamicWPTLoader(File inputfile, int buffersize) {
		iBufferSize = buffersize;
		fpInputFile = inputfile;
	}
	
	public int init() {
		//steps = new Step[iBufferSize];
		System.out.println("Initiating Dynamic loader... ");
		iCurMin = 0;
		iCurMax = 0;
		
		try {
			FileReader fr;
			BufferedReader br;
			String sCurLine;
			String[] saCurLine;
			int iCurLine = 1;
			fr = new FileReader(fpInputFile);	
			br = new BufferedReader(fr);
			
			int curStepNumber = 0;
			
			sCurLine = br.readLine(); /* read the first line */
			
			if(sCurLine == null) {
				br.close();
				return INFILE_EOFSTARTERROR;
			}
			
			saCurLine = sCurLine.split(";");
			if(saCurLine.length != 3) {
				br.close();
				return iCurLine;
			}
			
			int numSteps = Integer.parseInt(saCurLine[1]);
			dtimeCount = Double.parseDouble(saCurLine[2]);
			
			iLastStep=numSteps-1;
			System.out.println("Got stepcount: "+numSteps);
			steps = new Step[numSteps];
			
			iCurLine++;
			
			for(int i=0;i<numSteps;i++) {
				sCurLine = br.readLine(); /* read the first line of the block */
				
				if(sCurLine == null) {
					br.close();
					return INFILE_EOFSTEPERROR;
				}
				
				saCurLine = sCurLine.split(";");
				if(saCurLine.length != 2) {
					br.close();
					return iCurLine;
				}
				int numObjects = Integer.parseInt(saCurLine[1]);
				//System.out.println("Stepobject with #"+curStepNumber+" initialised");
				steps[curStepNumber++] = new Step(i,iCurLine);
				iCurLine++;
				
				//Masspoint_Sim[] tempMassP = new Masspoint_Sim[numObjects];
				
				for(int j=0;j<numObjects;j++) {
					sCurLine = br.readLine(); 
	
					if(sCurLine == null) {
						br.close();
						return INFILE_EOFOBJERROR;
					}
					
					saCurLine = sCurLine.split(";");
					if(saCurLine.length != 12) {
						br.close();
						return iCurLine;
					}
					else {
						/*
						int ID = Integer.parseInt(saCurLine[0]);
						double Mass = Double.parseDouble(saCurLine[1]);
						double Radius = Double.parseDouble(saCurLine[2]);
						double SpeedX = Double.parseDouble(saCurLine[3]);
						double SpeedY = Double.parseDouble(saCurLine[4]);
						double SpeedZ = Double.parseDouble(saCurLine[5]);
						double AccX = Double.parseDouble(saCurLine[6]);	//TODO not implemented yet
						double AccY = Double.parseDouble(saCurLine[7]); //TODO not implemented yet
						double AccZ = Double.parseDouble(saCurLine[8]); //TODO not implemented yet
						long PosX = Long.parseLong(saCurLine[9]);
						long PosY = Long.parseLong(saCurLine[10]);
						long PosZ = Long.parseLong(saCurLine[11]);
						//tempMassP[j] = new Masspoint_Sim(ID,Mass,Radius,SpeedX,SpeedY,SpeedZ,AccX,AccY,AccZ,PosX,PosY,PosZ);
						*/
					}
					iCurLine++;
				}
			}
			br.close();
		} catch(FileNotFoundException e) {
			return INFILE_FILENOTFOUND;
		} catch(IOException e) {
			return INFILE_READERROR;
		}
		return INFILE_NOERROR; /* no error detected */
	}
	
	private void loadSteps(int start, int stop) throws Exception {
		
		boolean alreadyLoadedAll = true;
		for(int i=start;i<=stop;i++) {
			if(steps[i].isLoaded()==false) {
				alreadyLoadedAll = false;
				break;
			}
		}
		if(alreadyLoadedAll) return;
		
		long stepline = steps[start].getWPTLineNumber();
		
		FileReader fr;
		BufferedReader br;
		String sCurLine;
		String[] saCurLine;
		fr = new FileReader(fpInputFile);	
		br = new BufferedReader(fr);
		
		for(long i=0;i<stepline-1;i++) { /* go to the first step */
			br.readLine();
		}
		
		for(int i=start;i<=stop;i++) {
			sCurLine = br.readLine(); /* read the first line of the block */
		
			//System.out.println("Stepheader: "+sCurLine);
			
			if(sCurLine == null) {
				br.close();
				throw new IOException();
			}
			
			saCurLine = sCurLine.split(";");
			if(saCurLine.length != 2) {
				br.close();
				throw new IOException();
			}
			int numObjects = Integer.parseInt(saCurLine[1]);
			
			Masspoint_Sim[] tempMassP = new Masspoint_Sim[numObjects];
			
			for(int j=0;j<numObjects;j++) {
				sCurLine = br.readLine(); 
				
				if(sCurLine == null) {
					br.close();
					throw new IOException();
				}
				
				if(!steps[i].isLoaded()) { /* if its not loaded ... load it! */
				
					saCurLine = sCurLine.split(";");
					if(saCurLine.length != 12) {
						br.close();
						throw new IOException();
					}
					else {
						int ID = Integer.parseInt(saCurLine[0]);
						double Mass = Double.parseDouble(saCurLine[1]);
						double Radius = Double.parseDouble(saCurLine[2]);
						double SpeedX = Double.parseDouble(saCurLine[3]);
						double SpeedY = Double.parseDouble(saCurLine[4]);
						double SpeedZ = Double.parseDouble(saCurLine[5]);
						double AccX = Double.parseDouble(saCurLine[6]);	//TODO not implemented yet
						double AccY = Double.parseDouble(saCurLine[7]); //TODO not implemented yet
						double AccZ = Double.parseDouble(saCurLine[8]); //TODO not implemented yet
						long PosX = Long.parseLong(saCurLine[9]);
						long PosY = Long.parseLong(saCurLine[10]);
						long PosZ = Long.parseLong(saCurLine[11]);
						tempMassP[j] = new Masspoint_Sim(ID,Mass,Radius,SpeedX,SpeedY,SpeedZ,AccX,AccY,AccZ,PosX,PosY,PosZ);
						
					}
					
				}
			}
			if(!steps[i].isLoaded()) { /* if its not loaded ... load it! */
				steps[i].setMasspoint_Sim(tempMassP);
			}
		}
		br.close();
	}
	
	private void prefetchSteps(int stepnumber) {
		// TODO check!
		//if(stepnumber < iCurMax-(iBufferSize/2) && stepnumber > iCurMin+1) {
		if(stepnumber < iCurMax && stepnumber > iCurMin) {
			/* no need to buffer something*/
			return;
		}
		
		
		if(stepnumber - iBufferSize < 0) {
			iCurMin = 0;
		}
		else {
			iCurMin = stepnumber - iBufferSize;
		}
		if(stepnumber + iBufferSize > iLastStep) {
			iCurMax = iLastStep;
		}
		else {
			iCurMax = stepnumber+iBufferSize;
		}
		
		//System.out.println("Prefetching steps "+iCurMin+" until "+iCurMax+"");
		
		for(int i=0;i<iCurMin;i++) steps[i].unloadMasspoints();
		for(int i=iCurMax+1;i<=iLastStep;i++) steps[i].unloadMasspoints();
		
		
		try {
			loadSteps(iCurMin, iCurMax);
		}
		catch(Exception e) {
			System.out.println("Error while fetching steps");
		}
		
	}
	
	public Step getStep(int stepnumber) {
		//System.out.println("- getStep("+stepnumber+") request");
		if(stepnumber>steps.length || stepnumber < 0) {
			throw new IndexOutOfBoundsException();
		}
		prefetchSteps(stepnumber);
		return steps[stepnumber];
	}

	public double getTimeCount() {
		return dtimeCount;
	}
}