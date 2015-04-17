import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.io.FileOutputStream;
/**
 * Created by xueliu on 4/15/15.
 */
public class FileUtilities {

    public static boolean isFileNameAPiece(String fileName) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        if(pattern.matcher(fileName).matches()){
            if (Integer.valueOf(fileName) < CfgPeeker.getBitsNumberOfBitField()){
                return true;
            }
        }
        return false;
    }

    public static boolean splitFileIntoPieces(String path, String fileName, Integer number) {
        Separator separator = new Separator();
        Integer blockSize=Integer.valueOf(CfgPeeker.getPieceSize());
        System.out.println(blockSize);
        if(separator.separatorFile(path, fileName, blockSize))
        {
            //System.out.println("succeed");
        }
        else
        {
            System.out.println("split file fail");
        }
        return true;
    }

    public static boolean isTransferComplete(String path){
        Integer fileCount = CfgPeeker.getBitsNumberOfBitField();
        for (int i=0;i<fileCount;i++){
            File f = new File(path+i);
            if(!f.exists()){
                return false;
            }
        }
        return true;
    }

    public static boolean combineFilesIntoOne(String path,String fileName){
        if(!isTransferComplete(path)){
            return false;
        }
        Combination combination = new Combination(path,fileName);
        combination.getFileAttribute(combination.srcDirectory);
        combination.CombFile();
        return true;
    }

    public static void deletePieces(String path){
        File dir = new File(path);
        if (dir.exists()){
            File[] files = dir.listFiles();
            for (File f : files){
                if(FileUtilities.isFileNameAPiece(f.getName())){
                    f.delete();
                }
            }
        }
    }

}


class Separator
{
    String FileName=null;
    Integer FileSize=0;
    Integer BlockNum=0;
    public Separator()
    {
    }
    private void getFileAttribute(String fileAndPath)
    {
        File file=new File(fileAndPath);
        FileName=file.getName();
        FileSize=(int) file.length();
    }
    private Integer getBlockNum(Integer blockSize)
    {
        Integer fileSize=FileSize;
        if(fileSize<=blockSize)
            return 1;
        else
        {
            if(fileSize%blockSize>0)
            {
                return fileSize/blockSize+1;
            }
            else
                return fileSize/blockSize;
        }
    }
    private String generateSeparatorFileName(String path,String fileName, Integer currentBlock)
    {
        return path+currentBlock;
    }
    private boolean writeFile(String fileAndPath,String fileSeparateName,Integer blockSize,Integer beginPos)
    {

        RandomAccessFile raf=null;
        FileOutputStream fos=null;
        byte[] bt=new byte[1024];
        Integer writeByte=0;
        int len=0;
        try
        {
            raf = new RandomAccessFile(fileAndPath,"r");
            raf.seek(beginPos);
            fos = new FileOutputStream(fileSeparateName);
            while((len=raf.read(bt))>0)
            {
                if(writeByte<blockSize)
                {
                    writeByte=writeByte+len;
                    if(writeByte<=blockSize)
                        fos.write(bt,0,len);
                    else
                    {
                        len=len-(int)(writeByte-blockSize);
                        fos.write(bt,0,len);
                    }
                }
            }
            fos.close();
            raf.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                if(fos!=null)
                    fos.close();
                if(raf!=null)
                    raf.close();
            }
            catch(Exception f)
            {
                f.printStackTrace();
            }
            return false;
        }
        return true;
    }
    boolean separatorFile(String path, String fileName,Integer blockSize)
    {
        getFileAttribute(path+fileName);
        BlockNum=getBlockNum(blockSize);
        if(BlockNum==1)
            blockSize=FileSize;
        Integer writeSize=0;
        Integer writeTotal=0;
        String FileCurrentNameAndPath=null;
        for(int i=0;i<BlockNum;i++)
        {
            if(i<BlockNum)
                writeSize=blockSize;
            else
                writeSize=FileSize-writeTotal;
            if(BlockNum==1)
                FileCurrentNameAndPath=(path+fileName+".bak");
            else
                FileCurrentNameAndPath=generateSeparatorFileName(path, fileName,i);
            if(!writeFile((path + fileName), FileCurrentNameAndPath, writeSize, writeTotal))
                return false;
            writeTotal=writeTotal+writeSize;
        }
        return true;
    }
    public static void main(String[] args)
    {
        Separator separator = new Separator();
        String path="d://";
        String fileName = "test.rar";
        Integer blockSize=30000;
        if(separator.separatorFile(path,fileName,blockSize))
        {
            System.out.println("succeed");
        }
        else
        {
            System.out.println("fail");
        }

    }
}

class Combination
{
    String srcDirectory=null;
    String[] separatedFiles;
    String[][] separatedFilesAndSize;
    int FileNum=0;
    String fileRealName="";
    public Combination(String path,String fileName)
    {
        srcDirectory=path;
        this.fileRealName = fileName;
    }
    private String getRealName(String sFileName)
    {
        StringTokenizer st=new StringTokenizer(sFileName,".");
        return st.nextToken()+"."+st.nextToken();
    }
    private long getFileSize(String FileName)
    {
        FileName=srcDirectory+FileName;
        return (new File(FileName).length());
    }
    void getFileAttribute(String drictory)
    {
        File file=new File(drictory);
        String[] files = file.list();
        for(int i = 0 ; i<files.length;i++){
            if(FileUtilities.isFileNameAPiece(files[i]) == false){
                files = removeArrayItem(files,i);
            }
        }
        separatedFiles=new String[files.length];
        separatedFiles=files;
        separatedFilesAndSize=new String[separatedFiles.length][2];
        Arrays.sort(separatedFiles);
        FileNum=separatedFiles.length;
        for(int i=0;i<FileNum;i++)
        {
            separatedFilesAndSize[i][0]=separatedFiles[i];
            separatedFilesAndSize[i][1]=String.valueOf(getFileSize(separatedFiles[i]));
        }
    }
    boolean CombFile()
    {
        RandomAccessFile raf=null;
        long alreadyWrite=0;
        FileInputStream fis=null;
        int len=0;
        byte[] bt=new byte[1024];
        try
        {
            raf = new RandomAccessFile(srcDirectory+fileRealName,"rw");
            for(int i=0;i<FileNum;i++)
            {
                raf.seek(alreadyWrite);
                fis=new FileInputStream(srcDirectory+separatedFilesAndSize[i][0]);
                while((len=fis.read(bt))>0)
                {
                    raf.write(bt,0,len);
                }
                fis.close();
                alreadyWrite=alreadyWrite+Long.parseLong(separatedFilesAndSize[i][1]);
            }
            raf.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                if(raf!=null)
                    raf.close();
                if(fis!=null)
                    fis.close();
            }
            catch (IOException f)
            {
                f.printStackTrace();
            }
            return false;
        }
        return true;
    }
    public static void main(String[] args)
    {
    }


    public static <T> T[] removeArrayItem(T[] arrs, int index) {
        int len = arrs.length;
        if(index < 0 || index >= len) {
            throw new IllegalArgumentException("index out of boundary");
        }
        List<T> list = new LinkedList<T>();
        for(int i = 0; i < len; i++) {
            if(i != index) {
                list.add(arrs[i]);
            }
        }
        arrs = list.toArray(arrs);
        return java.util.Arrays.copyOf(arrs, arrs.length - 1);
    }
}