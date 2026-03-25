package ch.usi.inf.bsc.sa4.lab02spring.model;

public record CreateObjectProperties(
    BoxContentType boxContentType
) {
    public static final CreateObjectProperties DEFAULT = new CreateObjectProperties(BoxContentType.EMPTY);
    
    public Content toBoxContent() {
        return boxContentType.toContent();
    }
}
