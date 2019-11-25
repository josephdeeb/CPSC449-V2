
public class UIPacket {
    public String nextUI;
    public String[] args;
    
    public UIPacket(String nextUI, String[] args) {
        this.nextUI = nextUI;
        this.args = args;
    }
    
    public UIPacket(String nextUI) {
        this.nextUI = nextUI;
        this.args = null;
    }
}
