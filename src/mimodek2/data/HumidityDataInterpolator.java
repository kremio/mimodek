package mimodek2.data;

import mimodek2.Configurator;

public class HumidityDataInterpolator extends DataInterpolator {

	public HumidityDataInterpolator(DataHandler dataHandler) {
		super("DATA_HUMIDITY", dataHandler);
	}
	
	/* (non-Javadoc)
	 * @see MimodekV2.data.DataInterpolator#update()
	 */
	@Override
	public void update(){
		lastUpdate = System.currentTimeMillis();
		lastValue = nextValue;
		HumidityLevelsEnum humidity = HumidityLevelsEnum.getHumidityLevelForScore(Configurator.getFloatSetting("DATA_HUMIDITY"));
		//System.out.println("Pollution level: "+pollution); 
		nextValue = TemperatureColorRanges.getRandomTemperatureInRange(humidity.getColorRange());
		//System.out.println("Pollution to temperature: "+nextValue);
	}
}
