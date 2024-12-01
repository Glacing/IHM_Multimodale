/*
 *  vocal_ivy -> Demonstration with ivy middleware
 * v. 1.2
 * 
 * (c) Ph. Truillet, October 2018-2019
 * Last Revision: 22/09/2020
 * Gestion de dialogue oral
 */
 
import fr.dgac.ivy.*;
import java.util.Iterator;

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

void setup()
{
  size(800,800);
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

void draw() {
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
String handleAction(String message){
  message=message.split("action=")[1];
  message=message.split(" ")[0];
  return message;
}

String handleForme(String message){
  message=message.split("form=")[1];
  message=message.split(" ")[0];
  return message;
}

String handleCouleur(String message){
  message=message.split("color=")[1];
  message=message.split(" ")[0];
  return message;
}

String handleLieu(String message){
  message=message.split("localisation=")[1];
  message=message.split(" ")[0];
  return message;
}

void handleDelete(){
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

void handleMove(){
  Iterator<Forme> iterator = listeFormes.iterator();
  while (iterator.hasNext()) {
    Forme f = iterator.next();
    if(f.isClicked(point1)){
      f.setLocation(point2);
    }
  }
}

void handleModify(){
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

void mouseClicked() {
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
