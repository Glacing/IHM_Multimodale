public class Commande {
  private String action;
  private String forme;
  private String couleur;
  private String lieu;
  
  public Commande(String action, String forme, String couleur, String lieu){
    this.action = action;
    this.forme = forme;
    this.couleur = couleur;
    this.lieu = lieu;
  }
  
  public void setAction(String action){
    this.action = action;
  }
  public void setForme(String forme){
    this.forme = forme;
  }
  public void setCouleur(String couleur){
    this.couleur = couleur;
  }
  public void setLieu(String lieu){
    this.lieu = lieu;
  }
  public String getAction(){
    return this.action;
  }
  public String getForme(){
    return this.forme;
  }
  
  public String getCouleur(){
    return this.couleur;
  }
  
  public String toString(){
    String retour="";
    retour+= "Action : " + action + " - Forme : " + forme + " - Couleur : " +couleur + " - Lieu : " + lieu;
    return retour;
  }
  
 
  public Forme commandToForme(int x, int y){
    Point p = new Point(x,y);
    Forme forme= null;
    color c = color(0,0,0);
    if (this.couleur == null){
      this.couleur = "DARK"; 
    }
    switch(this.couleur){
      case "RED":
        c = color(255,0,0);
        break;
      case "ORANGE":
        c = color(255,165,0);
        break;
      case "YELLOW":
        c = color(255,255,0);
        break;
      case "GREEN":
        c = color(0,255,0);
        break;
      case "BLUE":
        c = color(0,0,255);
        break;
      case "PURPLE":
        c = color(255,0,255);
        break;
      case "DARK":
        c = color(0,0,0);
        break;
      case "PINK":
        c = color(255,102,178);
        break;
    }
    
    switch(this.forme){
      case "DIAMOND":
        forme = new Losange(p);
        break;
      case "RECTANGLE":
        forme = new Rectangle(p);
        break;
      case "TRIANGLE":
        forme = new Triangle(p);
        break;
      case "CIRCLE":
        forme = new Cercle(p);
        break;
    }
    
    forme.setColor(c);
    
    return forme;
  }
  
  public String convertColor(color c){
    String rgb = (int) red(c) + "," + (int) green(c) + "," + (int) blue(c);
    switch(rgb){
      case "255,0,0":
        return "RED";
      case "255,165,0":
        return "ORANGE";
      case "255,255,0":
        return "YELLOW";
      case "0,255,0":
        return "GREEN";
      case "0,0,255":
        return "BLUE";
      case "255,0,255":
        return "PURPLE";
      case "0,0,0":
        return "DARK";
      case "255,102,178":
        return "PINK";
    }
    return "DARK";
  }
}
