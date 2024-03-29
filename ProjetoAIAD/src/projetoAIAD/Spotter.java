package projetoAIAD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.poi.util.SystemOutLogger;

import jade.lang.acl.ACLMessage;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.*;

public class Spotter extends Agent{

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private Random random = new Random();
	private Double angle = null;
	private final double randomness = 0.05;
	private int id;
	private Object mina=null;	
	private int raio;
	private boolean stopped = false;
	
	private boolean alreadySent=false;


	Spotter(int id,int raio)
	{
		super();
		this.id = id;
		this.raio=raio;
	}


	@Override
	public void setup(){
		super.setup();
		Context context = ContextUtils.getContext(this);
		space = (ContinuousSpace<Object>)context.getProjection("space");
		NdPoint pt = space.getLocation(this);
		grid = (Grid<Object>)context.getProjection("grid");
		grid.moveTo(this, (int) pt.getX(), (int) pt.getY());
		
		/*
		if(this.getClass().isAssignableFrom(Spotter.class)){
			
		}
		*/
		
		addBehaviour(new CyclicBehaviour(this) 
        {
             public void action() 
             {
            	 ACLMessage msg;
                 while ((msg = receive())!=null){
                    // System.out.println("SPOTTER RECEIVED:" + msg.getContent());
                  
                     if(msg.getContent().equals("ready") && !alreadySent){
                    	 ACLMessage res = new ACLMessage(ACLMessage.INFORM);
                    	 res.setContent("yes");
                    	 res.addReceiver( msg.getSender() );
            	 	     send(res);
            	 	     alreadySent=true;
            	 	     mina=null;
            	       	 stopped=false;
            	 	   
                     }else if(msg.getContent().equals("ready") && alreadySent){
                    	 
                    	 ACLMessage res = new ACLMessage(ACLMessage.INFORM);
                    	 res.setContent("no");
                    	 res.addReceiver( msg.getSender() );
            	 	     send(res);
                     }
                     
                     
                     
                     
                 }
             }
        });
	}

	
	@ScheduledMethod(start = 2, interval = 1)
	public void stepSpotter() {
		
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

		NdPoint myPoint = space.getLocation(this);
		ContinuousWithin<Object> t = new ContinuousWithin<Object>(space, (Object)this, raio);
		Iterator<Object> iterador = t.query().iterator();
		NdPoint minepoint = null;
		Object elemento=null;
		
		while(iterador.hasNext())
		{
			 elemento = iterador.next();
			if(elemento instanceof Mine)
			{		
				
				if(!stopped && !((Mine) elemento).explored ){
						    minepoint = space.getLocation(elemento);
							mina=elemento;
			}
		
			}else if(elemento instanceof Producer && !alreadySent && mina!=null && stopped){
				
			
					 double x = space.getLocation(mina).getX();
					 double y = space.getLocation(mina).getY();
					 
	    			 
					 msg.setContent( x+" "+y+" "+ ((Mine) mina).getID() );
	    			 msg.addReceiver( new AID( "Producer " + ((Producer) elemento).getId(), AID.ISLOCALNAME) );
	    	 	     send(msg);
			
			}
		}
		
		
		if(minepoint!=null && myPoint!=null){
			if(isOnTopMine(minepoint,myPoint)){
				stopped=true;

				alreadySent=false;
				((Mine) mina).explored=true;
				
				
			}else{
				moveTowards(minepoint);
			}
		}
		else if(stopped){
			
		}
		else{
			normalMovement();
		}
		
	}

	public boolean isOnTopMine(NdPoint mine, NdPoint mypoint){
		
		return (Math.abs((int)mine.getX()-(int)mypoint.getX())<2 && Math.abs((int)mine.getY()-(int)mypoint.getY())<2);
		
	}

	public void normalMovement(){
		double rand = random.nextDouble();
		if (angle != null && rand > randomness )
			moveByAngle(this.angle);
		else if (angle != null && rand > randomness )
			moveByAngle(this.angle + 0.2);
		else if (angle != null && rand > randomness)
			moveByAngle(this.angle - 0.2);
		else
		{
			List<NdPoint> sites = findEmptySites();
			if (!sites.isEmpty())
			{
				moveTowards(sites.get(0));
			}
		}
	}

	public void moveTowards(NdPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		if (!pt.equals(myPoint)) {
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			moveByAngle(SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint));
		}
	}

	public void moveByAngle(double angle)
	{
		this.angle = angle;
		space.moveByVector(this, 1, angle, 0);
		NdPoint myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}



	private List<NdPoint> findEmptySites(){
		List<NdPoint> emptySites = new ArrayList<NdPoint>();
		NdPoint pt = space.getLocation(this);
		double height = space.getDimensions().getHeight();
		double width = space.getDimensions().getWidth();

		for (int difx = -1; difx <= 1; difx++)
		{
			for (int dify = -1; dify <= 1; dify++)
			{
				if (difx == 0 && dify == 0) continue;
				double newx = pt.getX() + difx;
				double newy = pt.getY() + dify;

				emptySites.add(new NdPoint(newx, newy));
			}
		}

		Collections.shuffle(emptySites);
		return emptySites;
	}
}
