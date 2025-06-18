package com.example.telehotel.data.model;

/*public class Capacidad {
    private Integer adultos;
    private Integer ninos;

    // Constructores
    public Capacidad() {}

    public Capacidad(Integer adultos, Integer ninos) {
        this.adultos = adultos;
        this.ninos = ninos;
    }

    // Getters y Setters
    public Integer getAdultos() { return adultos; }
    public void setAdultos(Integer adultos) { this.adultos = adultos; }

    public Integer getNinos() { return ninos; }
    public void setNinos(Integer ninos) { this.ninos = ninos; }

}*/



public class Capacidad {
    private Integer adultos;
    private Integer ninos;

    // Constructores
    public Capacidad() {}

    public Capacidad(Integer adultos, Integer ninos) {
        this.adultos = adultos;
        this.ninos = ninos;
    }

    // Getters y Setters
    public Integer getAdultos() {
        return adultos;
    }

    public void setAdultos(Integer adultos) {
        this.adultos = adultos;
    }

    public Integer getNinos() {
        return ninos;
    }

    public void setNinos(Integer ninos) {
        this.ninos = ninos;
    }

    // ✅ AGREGADOS: Métodos de utilidad
    public int getTotal() {
        int totalAdultos = (adultos != null) ? adultos : 0;
        int totalNinos = (ninos != null) ? ninos : 0;
        return totalAdultos + totalNinos;
    }

    public boolean esValida() {
        return (adultos != null && adultos > 0) || (ninos != null && ninos > 0);
    }

    public String getTextoCapacidad() {
        int totalAdultos = (adultos != null) ? adultos : 0;
        int totalNinos = (ninos != null) ? ninos : 0;
        return totalAdultos + " adultos, " + totalNinos + " niños";
    }

    // ✅ AGREGADO: Para compatibilidad con validaciones
    public boolean puedeAcomodar(int requeridoAdultos, int requeridoNinos) {
        int disponibleAdultos = (adultos != null) ? adultos : 0;
        int disponibleNinos = (ninos != null) ? ninos : 0;

        return disponibleAdultos >= requeridoAdultos && disponibleNinos >= requeridoNinos;
    }

    @Override
    public String toString() {
        int totalAdultos = (adultos != null) ? adultos : 0;
        int totalNinos = (ninos != null) ? ninos : 0;
        return totalAdultos + " adultos, " + totalNinos + " niños";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Capacidad capacidad = (Capacidad) obj;

        if (adultos != null ? !adultos.equals(capacidad.adultos) : capacidad.adultos != null)
            return false;
        return ninos != null ? ninos.equals(capacidad.ninos) : capacidad.ninos == null;
    }

    @Override
    public int hashCode() {
        int result = adultos != null ? adultos.hashCode() : 0;
        result = 31 * result + (ninos != null ? ninos.hashCode() : 0);
        return result;
    }
}