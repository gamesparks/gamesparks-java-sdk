package com.gamesparks.sdk.realtime;

class ActionCommand implements IRTCommand {
	public static ObjectPool<ActionCommand>pool = new ObjectPool<ActionCommand>(new ObjectPoolCreatorEvent<ActionCommand>() {
		@Override
		public ActionCommand onEvent() {
			return new ActionCommand();
		}
	});
	
	private Action action;
	
	public ActionCommand() {
		action = null;
	}
	
	public ActionCommand configure(Action action) {
		this.action = action;
		
		return this;
	}
	
	@Override
	public void execute() {
		action.onEvent();
		
		pool.push(this);
	}
}
