package cz.wincor.pnc.types;

public enum HostTrxMessageEnum {
    RSS("|>RSS|"),AIC("|>AIC|"),CIF("|>CIF|"),PS("|>PS|"),ATA("|>ATA|"),WSCC("|>WS|"),NOT_SUPPORTED("");
    
    private String pattern;

    private HostTrxMessageEnum(String name) {
        this.pattern = name;
    }

    public String getPattern() {
        return pattern;
    }
    
    
}
