package ch.usi.inf.bsc.sa4.lab02spring.model;

public record ContentType(String value) {
    public ContentType{
        if(value == null || !value.equals("coin"))
        {
            throw new IllegalArgumentException();
        }
    }
    
}
