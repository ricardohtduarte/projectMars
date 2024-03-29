package projetoAIAD;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;

public class MarsBuilder implements ContextBuilder<Object> {
	
	final public static int WIDTH = 50;
	final public static int HEIGHT = 50;

	private Random random = new Random();
	
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("mars");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(),
				WIDTH, HEIGHT);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, WIDTH, HEIGHT));
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int mineCount =  (Integer) params.getValue("mine_count");
		int mineQuantityMax =  (Integer) params.getValue("quantity");
		for(int i = 0; i < mineCount; i++){
			Mine mine = new Mine(i+1,random.nextInt(mineQuantityMax)+1);
			context.add(mine);
		}
		
		//base
		
		Base base= new Base(1);
		
		context.add(base);
		
	
		
		
		return context;
	}
}
