package net.gbtec.pruebatecnicabackend.model;

public enum EmailState {
    ESTADO_NO_VÁLIDO(0),
    ENVIADO(1),
    BORRADOR(2),
    ELIMINADO(3),
    SPAM(4);

    private final int value;

    private EmailState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EmailState fromValue(int value) {
        for (EmailState state : EmailState.values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Valor no válido para EmailState: " + value);
    }
}
