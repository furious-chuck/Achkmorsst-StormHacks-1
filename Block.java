public class Block { // louie/dingus' thingus thing
    int blockType = 0;

    //some variables of the block
    int[] ints;
    String[] strings;
    Block[] children;


    //block definers
    Block(int newType) {
        blockType = newType;
    }

    Block(int newType, int[] defaultInts) {
        blockType = newType;
        ints = defaultInts;
    }

    Block(int newType, String[] defaultStrings) {
        blockType = newType;
        strings = defaultStrings;
    }

    Block(int newType, int[] defaultInts, String[] defaultStrings) {
        blockType = newType;
        ints = defaultInts;
        strings = defaultStrings;
    }

    void Process() { //execute the block
        switch(blockType) {
            case Consts.EXAMPLE_TYPE:
                break;
            default:
        }
        for (Block child: children) {
            child.Process();
        }
    }

    void Draw() {
        //draws the block, idk how to do this
        switch(blockType) {
            case Consts.EXAMPLE_TYPE:
                break;
            default:
        }
        for (Block child: children) {
            child.Draw();
        }
    }

}
