/* autogenerated by Processing revision 1290 on 2024-12-01 */
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import fr.dgac.ivy.*;
import fr.dgac.ivy.tools.*;
import gnu.getopt.*;

import fr.dgac.ivy.*;
import java.util.Iterator;
import java.awt.Point;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class dessert extends PApplet {

/*
 *  vocal_ivy -> Demonstration with ivy middleware
 * v. 1.2
 * 
 * (c) Ph. Truillet, October 2018-2019
 * Last Revision: 22/09/2020
 * Gestion de dialogue oral
 */
 



// data

Ivy bus;
Ivy busIcar;
PFont f;
String message= "";
String messageIcar= "";

int state;
public static final int INIT = 0;
public static final int ATTENTE = 1;
public static final int TEXTE = 2;
public static final int CONCEPT = 3;
public static final int NON_RECONNU = 4;


private Commande commande;
int stateCommande;

public static final int INIT_COMMANDE = 0;
public static final int ORDRE_DONNE = 1;
public static final int CLICK_DONNE = 2;
public static final int CLICK2_DONNE = 3;

ArrayList<Forme> listeFormes;

public Point point1;
public Point point2;

public void setup()
{
  /* size commented out by preprocessor */;
  fill(0,0,0);
  state = INIT;
  commande = new Commande(null,null,null,null);
  stateCommande = 0;
  listeFormes = new ArrayList<>();
  try
  {
    bus = new Ivy("sra5", " sra_tts_bridge is ready", null);
    busIcar = new Ivy("ICAR", " ICAR_bridge is ready", null);
    bus.start("127.255.255.255:2010");
    busIcar.start("127.255.255.255:2010");
    
    bus.bindMsg("^sra5 Text=(.*) Confidence=.*", new IvyMessageListener()
    {
      public void receive(IvyClient client,String[] args)
      {
        message = "Vous avez dit : " + args[0];
        state = TEXTE;
      }        
    });
    
    bus.bindMsg("^sra5 Parsed=(.*) Confidence=(.*) NP=.*", new IvyMessageListener()
    {
      public void receive(IvyClient client,String[] args)
      {
        message = "Vous avez prononcé les concepts : " + args[0] + " avec un taux de confiance de " + args[1];
        state = CONCEPT;
      }        
    });
    
    bus.bindMsg("^sra5 Event=Speech_Rejected", new IvyMessageListener()
    {
      public void receive(IvyClient client,String[] args)
      {
        message = "Malheureusement, je ne vous ai pas compris"; 
        state = NON_RECONNU;
      }        
    });   
    
    busIcar.bindMsg("^ICAR Gesture=(.*)", new IvyMessageListener()
    {
      public void receive(IvyClient client,String[] args)
      {
        messageIcar = args[0];
      }        
    });
  }
  catch (IvyException ie)
  {
  }
}

public void draw() {
    background(255);
    
    if(message!=null){
      stateCommande = ORDRE_DONNE;
      // Gestion parole
      if (!message.contains("action=undefined") && message.contains("action=")) {
          commande.setAction(handleAction(message));
      }
      if (!message.contains("form=undefined") && message.contains("form=")) {
          commande.setForme(handleForme(message));
      }
      if (!message.contains("color=undefined") && message.contains("color=")) {
          commande.setCouleur(handleCouleur(message));
      }
      if (!message.contains("localisation=undefined") && message.contains("localisation=")) {
          commande.setLieu(handleLieu(message));
      }
    }
    
    if(commande.getAction() != null){
      switch (commande.getAction()) {
          case "CREATE":
              if(commande.getForme()!=null && commande.getForme()!="" && stateCommande >= CLICK_DONNE){
                //handleCreate()
                if(point2!=null){
                  listeFormes.add(commande.commandToForme(point2.x,point2.y));
                } else {
                  listeFormes.add(commande.commandToForme(point1.x,point1.y));
                }
                commande = new Commande(null, null, null, null);
                point1 = null;
                point2 = null;
                stateCommande = INIT_COMMANDE;
              }
              break;
          case "DELETE":
              if(stateCommande >= CLICK_DONNE){
                handleDelete();
                commande = new Commande(null, null, null, null);
                point1 = null;
                point2 = null;
                stateCommande = INIT_COMMANDE;
              }
              break;
          case "MOVE":
              if(point1!= null && point2!=null && stateCommande >= CLICK_DONNE){
                handleMove();
                commande = new Commande(null, null, null, null);
                point1 = null;
                point2 = null;
                stateCommande = INIT_COMMANDE;
              }
              break;
         case "MODIFY":
             if(stateCommande >= CLICK_DONNE){
               handleModify();
               commande = new Commande(null, null, null, null);
               point1 = null;
               point2 = null;
               stateCommande = INIT_COMMANDE;
             }
             break;
          
      }
    message = null;
    }
     
     
    if(messageIcar != null) {
      commande.setForme(messageIcar.toUpperCase());
      messageIcar = null;
    }
    // Affichage
    for (Forme f : listeFormes) {
        f.update();
    }

    state = ATTENTE;
}


//Methodes handleCommande
public String handleAction(String message){
  message=message.split("action=")[1];
  message=message.split(" ")[0];
  return message;
}

public String handleForme(String message){
  message=message.split("form=")[1];
  message=message.split(" ")[0];
  return message;
}

public String handleCouleur(String message){
  message=message.split("color=")[1];
  message=message.split(" ")[0];
  return message;
}

public String handleLieu(String message){
  message=message.split("localisation=")[1];
  message=message.split(" ")[0];
  return message;
}

public void handleDelete(){
  Iterator<Forme> iterator = listeFormes.iterator();
  while (iterator.hasNext()) {
    Forme f = iterator.next();
    if(point2!=null) {
      if (f.isClicked(point2)) {
        iterator.remove(); // Suppression sécurisée via l'itérateur
      }
    } else if(point1!=null) {
      if (f.isClicked(point1)) {
        iterator.remove(); // Suppression sécurisée via l'itérateur
      }
    }
  }
}

public void handleMove(){
  Iterator<Forme> iterator = listeFormes.iterator();
  while (iterator.hasNext()) {
    Forme f = iterator.next();
    if(f.isClicked(point1)){
      f.setLocation(point2);
    }
  }
}

public void handleModify(){
  Iterator<Forme> iterator = listeFormes.iterator();
  int index = 0;
  while (iterator.hasNext()) {
    Forme f = iterator.next();
    if(f.isClicked(point1)){
      if(commande.getCouleur()==null){
        commande.setCouleur(commande.convertColor(f.getColor()));
      }
      if(commande.getForme()==null){
        if(f instanceof Cercle) {
          commande.setForme("CIRCLE");
        } else if(f instanceof Losange) {
          commande.setForme("DIAMOND");
        } else if(f instanceof Rectangle) {
          commande.setForme("RECTANGLE");
        } else if(f instanceof Triangle) {
          commande.setForme("TRIANGLE");
        }
      }
      listeFormes.set(index,commande.commandToForme(f.getLocation().x,f.getLocation().y));
    }
    index++;
  }
}

public void mouseClicked() {
  if(stateCommande >= ORDRE_DONNE) {
    if(point1 == null){
      point1 = new Point(mouseX,mouseY);
      stateCommande = CLICK_DONNE;
    } else if(point2==null){
      point2= new Point(mouseX,mouseY);
      stateCommande = CLICK2_DONNE;
    } else {
      point1 = point2;
      point2 = new Point(mouseX,mouseY);
      stateCommande = CLICK2_DONNE;
    }
  }
}
/*
 * Classe Cercle
 */ 
 
public class Cercle extends Forme {
  
  int rayon;
  
  public Cercle(Point p) {
    super(p);
    this.rayon=80;
  }
   
  public void update() {
    fill(this.c);
    circle((int) this.origin.getX(),(int) this.origin.getY(),this.rayon);
  }  
   
  public boolean isClicked(Point p) {
    // vérifier que le cercle est cliqué
   PVector OM= new PVector( (int) (p.getX() - this.origin.getX()),(int) (p.getY() - this.origin.getY())); 
   if (OM.mag() <= this.rayon/2)
     return(true);
   else 
     return(false);
  }
  
  protected double perimetre() {
    return(2*PI*this.rayon);
  }
  
  protected double aire(){
    return(PI*this.rayon*this.rayon);
  }
}
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
    int c = color(0,0,0);
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
  
  public String convertColor(int c){
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
/*
 * Enumération de a Machine à Etats (Finite State Machine)
 *
 *
 */
 
public enum FSM {
  INITIAL, /* Etat Initial */ 
  AFFICHER_FORMES, 
  DEPLACER_FORMES_SELECTION,
  DEPLACER_FORMES_DESTINATION
}
/*****
 * Création d'un nouvelle classe objet : Forme (Cercle, Rectangle, Triangle
 * 
 * Date dernière modification : 28/10/2019
 */

abstract class Forme {
 Point origin;
 int c;
 
 Forme(Point p) {
   this.origin=p;
   this.c = color(127);
 }
 
 public void setColor(int c) {
   this.c=c;
 }
 
 public int getColor(){
   return(this.c);
 }
 
 public abstract void update();
 
 public Point getLocation() {
   return(this.origin);
 }
 
 public void setLocation(Point p) {
   this.origin = p;
 }
 
 public abstract boolean isClicked(Point p);
 
 // Calcul de la distance entre 2 points
 protected double distance(Point A, Point B) {
    PVector AB = new PVector( (int) (B.getX() - A.getX()),(int) (B.getY() - A.getY())); 
    return(AB.mag());
 }
 
 protected abstract double perimetre();
 protected abstract double aire();
}
/*
 * Classe Losange
 */ 
 
public class Losange extends Forme {
  Point A, B,C,D;
  
  public Losange(Point p) {
    super(p);
    // placement des points
    A = new Point();    
    A.setLocation(p);
    B = new Point();    
    B.setLocation(A);
    C = new Point();  
    C.setLocation(A);
    D = new Point();
    D.setLocation(A);
    B.translate(40,60);
    D.translate(-40,60);
    C.translate(0,120);
  }
  
  public void setLocation(Point p) {
      super.setLocation(p);
      // redéfinition de l'emplacement des points
      A.setLocation(p);   
      B.setLocation(A);  
      C.setLocation(A);
      D.setLocation(A);
      B.translate(40,60);
      D.translate(-40,60);
      C.translate(0,120);   
  }
  
  public void update() {
    fill(this.c);
    quad((float) A.getX(), (float) A.getY(), (float) B.getX(), (float) B.getY(), (float) C.getX(), (float) C.getY(),  (float) D.getX(),  (float) D.getY());
  }  
  
  public boolean isClicked(Point M) {
    // vérifier que le losange est cliqué
    // aire du rectangle AMD + AMB + BMC + CMD = aire losange  
    if (round( (float) (aire_triangle(A,M,D) + aire_triangle(A,M,B) + aire_triangle(B,M,C) + aire_triangle(C,M,D))) == round((float) aire()))
      return(true);
    else 
      return(false);  
  }
  
  protected double perimetre() {
    //
    PVector AB= new PVector( (int) (B.getX() - A.getX()),(int) (B.getY() - A.getY())); 
    PVector BC= new PVector( (int) (C.getX() - B.getX()),(int) (C.getY() - B.getY())); 
    PVector CD= new PVector( (int) (D.getX() - C.getX()),(int) (D.getY() - C.getY())); 
    PVector DA= new PVector( (int) (A.getX() - D.getX()),(int) (A.getY() - D.getY())); 
    return( AB.mag()+BC.mag()+CD.mag()+DA.mag()); 
  }
  
  protected double aire(){
    PVector AC= new PVector( (int) (C.getX() - A.getX()),(int) (C.getY() - A.getY())); 
    PVector BD= new PVector( (int) (D.getX() - B.getX()),(int) (D.getY() - B.getY())); 
    return((AC.mag()*BD.mag())/2);
  } 
  
  private double perimetre_triangle(Point I, Point J, Point K) {
    //
    PVector IJ= new PVector( (int) (J.getX() - I.getX()),(int) (J.getY() - I.getY())); 
    PVector JK= new PVector( (int) (K.getX() - J.getX()),(int) (K.getY() - J.getY())); 
    PVector KI= new PVector( (int) (I.getX() - K.getX()),(int) (I.getY() - K.getY())); 
    
    return( IJ.mag()+JK.mag()+KI.mag()); 
  }
   
  // Calcul de l'aire d'un triangle par la méthode de Héron 
  private double aire_triangle(Point I, Point J, Point K){
    double s = perimetre_triangle(I,J,K)/2;
    double aire = s*(s-distance(I,J))*(s-distance(J,K))*(s-distance(K,I));
    return(sqrt((float) aire));
  }
}
/*
 * Palette Graphique - prélude au projet multimodal 3A SRI
 * 4 objets gérés : cercle, rectangle(carré), losange et triangle
 * (c) 05/11/2019
 * Dernière révision : 28/04/2020
 */
 


ArrayList<Forme> formes; // liste de formes stockées
FSM mae; // Finite Sate Machine
int indice_forme;
PImage sketch_icon;



// fonction d'affichage des formes m
public void affiche() {
  background(255);
  /* afficher tous les objets */
  for (int i=0;i<formes.size();i++) // on affiche les objets de la liste
    (formes.get(i)).update();
}

/*
void mousePressed() { // sur l'événement clic
  Point p = new Point(mouseX,mouseY);
  
  switch (mae) {
    case AFFICHER_FORMES:
      for (int i=0;i<formes.size();i++) { // we're trying every object in the list
        // println((formes.get(i)).isClicked(p));
        if ((formes.get(i)).isClicked(p)) {
          (formes.get(i)).setColor(color(random(0,255),random(0,255),random(0,255)));
        }
      } 
      break;
      
   case DEPLACER_FORMES_SELECTION:
     for (int i=0;i<formes.size();i++) { // we're trying every object in the list        
        if ((formes.get(i)).isClicked(p)) {
          indice_forme = i;
          mae = FSM.DEPLACER_FORMES_DESTINATION;
        }         
     }
     if (indice_forme == -1)
       mae= FSM.AFFICHER_FORMES;
     break;
     
   case DEPLACER_FORMES_DESTINATION:
     if (indice_forme !=-1)
       (formes.get(indice_forme)).setLocation(new Point(mouseX,mouseY));
     indice_forme=-1;
     mae=FSM.AFFICHER_FORMES;
     break;
     
    default:
      break;
  }
}*/


/*
void keyPressed() {
  Point p = new Point(mouseX,mouseY);
  switch(key) {
    case 'r':
      Forme f= new Rectangle(p);
      formes.add(f);
      mae=FSM.AFFICHER_FORMES;
      break;
      
    case 'c':
      Forme f2=new Cercle(p);
      formes.add(f2);
      mae=FSM.AFFICHER_FORMES;
      break;
    
    case 't':
      Forme f3=new Triangle(p);
      formes.add(f3);
       mae=FSM.AFFICHER_FORMES;
      break;  
      
    case 'l':
      Forme f4=new Losange(p);
      formes.add(f4);
      mae=FSM.AFFICHER_FORMES;
      break;    
      
    case 'm' : // move
      mae=FSM.DEPLACER_FORMES_SELECTION;
      break;
  }
}*/
/*
 * Classe Rectangle
 */ 
 
public class Rectangle extends Forme {
  
  int longueur;
  
  public Rectangle(Point p) {
    super(p);
    this.longueur=60;
  }
   
  public void update() {
    fill(this.c);
    square((int) this.origin.getX(),(int) this.origin.getY(),this.longueur);
  }  
  
  public boolean isClicked(Point p) {
    int x= (int) p.getX();
    int y= (int) p.getY();
    int x0 = (int) this.origin.getX();
    int y0 = (int) this.origin.getY();
    
    // vérifier que le rectangle est cliqué
    if ((x>x0) && (x<x0+this.longueur) && (y>y0) && (y<y0+this.longueur))
      return(true);
    else  
      return(false);
  }
  
  // Calcul du périmètre du carré
  protected double perimetre() {
    return(this.longueur*4);
  }
  
  protected double aire(){
    return(this.longueur*this.longueur);
  }
}
/*
 * Classe Triangle
 */ 
 
public class Triangle extends Forme {
  Point A, B,C;
  public Triangle(Point p) {
    super(p);
    // placement des points
    A = new Point();    
    A.setLocation(p);
    B = new Point();    
    B.setLocation(A);
    C = new Point();    
    C.setLocation(A);
    B.translate(40,60);
    C.translate(-40,60);
  }
  
    public void setLocation(Point p) {
      super.setLocation(p);
      // redéfinition de l'emplacement des points
      A.setLocation(p);   
      B.setLocation(A);  
      C.setLocation(A);
      B.translate(40,60);
      C.translate(-40,60);   
  }
  
  public void update() {
    fill(this.c);
    triangle((float) A.getX(), (float) A.getY(), (float) B.getX(), (float) B.getY(), (float) C.getX(), (float) C.getY());
  }  
  
  public boolean isClicked(Point M) {
    // vérifier que le triangle est cliqué
    
    PVector AB= new PVector( (int) (B.getX() - A.getX()),(int) (B.getY() - A.getY())); 
    PVector AC= new PVector( (int) (C.getX() - A.getX()),(int) (C.getY() - A.getY())); 
    PVector AM= new PVector( (int) (M.getX() - A.getX()),(int) (M.getY() - A.getY())); 
    
    PVector BA= new PVector( (int) (A.getX() - B.getX()),(int) (A.getY() - B.getY())); 
    PVector BC= new PVector( (int) (C.getX() - B.getX()),(int) (C.getY() - B.getY())); 
    PVector BM= new PVector( (int) (M.getX() - B.getX()),(int) (M.getY() - B.getY())); 
    
    PVector CA= new PVector( (int) (A.getX() - C.getX()),(int) (A.getY() - C.getY())); 
    PVector CB= new PVector( (int) (B.getX() - C.getX()),(int) (B.getY() - C.getY())); 
    PVector CM= new PVector( (int) (M.getX() - C.getX()),(int) (M.getY() - C.getY())); 
    
    if ( ((AB.cross(AM)).dot(AM.cross(AC)) >=0) && ((BA.cross(BM)).dot(BM.cross(BC)) >=0) && ((CA.cross(CM)).dot(CM.cross(CB)) >=0) ) { 
      return(true);
    }
    else
      return(false);
  }
  
  protected double perimetre() {
    //
    PVector AB= new PVector( (int) (B.getX() - A.getX()),(int) (B.getY() - A.getY())); 
    PVector AC= new PVector( (int) (C.getX() - A.getX()),(int) (C.getY() - A.getY())); 
    PVector BC= new PVector( (int) (C.getX() - B.getX()),(int) (C.getY() - B.getY())); 
    
    return( AB.mag()+AC.mag()+BC.mag()); 
  }
   
  // Calcul de l'aire du triangle par la méthode de Héron 
  protected double aire(){
    double s = perimetre()/2;
    double aire = s*(s-distance(B,C))*(s-distance(A,C))*(s-distance(A,B));
    return(sqrt((float) aire));
  }
}


  public void settings() { size(800, 800); }

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dessert" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
