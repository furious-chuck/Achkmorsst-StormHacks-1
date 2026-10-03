public class Block { // louie/dingus' thingus thing
    int blockType = 0;
    int[] ints;
    String[] strings;


    Block(int newType) {
        blockType = newType;
    }

    void Process() {
        switch(blockType) {
            case Consts.EXAMPLE_TYPE:
                break;
            default:
        }
    }

}
