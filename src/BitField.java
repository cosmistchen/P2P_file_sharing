import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by xueliu on 4/15/15.
 */
public class BitField {

    public static boolean[] getBitFieldByPeerID(String id){
        boolean[] bools = new boolean[CfgPeeker.getBitsNumberOfBitField()];
        File dir = new File("peer" + id);
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            File[] files = dir.listFiles();
            for (File f : files) {
                if ((!f.getName().equals(CfgPeeker.getFileName())) && FileUtilities.isFileNameAPiece(f.getName())) {
                    bools[Integer.valueOf(f.getName())] = true;
                }
            }
        }
        return bools;
    }

    public static boolean bitFieldDiffer(boolean[] myBitField, boolean[] serverBitField){
        for(int i=0; i<myBitField.length;i++){
            if(myBitField[i] == false && serverBitField[i] == true){
                return true;
            }
        }
        return false;
    }

    public static boolean[] byteArrayToBooleanArrayForBitField(byte[] bytes) {
        boolean[] bools = new boolean[CfgPeeker.getBitsNumberOfBitField()];
        for(int i=0;i<bools.length;i++){
            if(bytes[i/8] == (bytes[i/8] | 1<<(7-i%8))){
                bools[i] = true;
            }else{
                bools[i] = false;
            }
        }
        return bools;
    }

    public static Integer getRandomDiffIndex(boolean[] myBitField, boolean[] serverBitField) {
        ArrayList<Integer> indexs = new ArrayList<Integer>();
        for(Integer i=0; i<myBitField.length;i++){
            if(myBitField[i] == false && serverBitField[i] == true){
                indexs.add(i);
            }
        }

        if(!indexs.isEmpty()){
            Random random = new Random();
            Integer index = random.nextInt(indexs.size());
            return indexs.get(index);
        }else{
            return -1;
        }
    }
}
