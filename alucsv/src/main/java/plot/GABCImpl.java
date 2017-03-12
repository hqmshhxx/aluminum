package plot;

public class GABCImpl extends GABC{
	
	double lb = -100.0;
	
	double ub = 100.0;

	@Override
	public double calculateFunction(double[] sol) {
		// TODO Auto-generated method stub
		return sphere(sol);
	}
	
	double sphere(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < dimension; j++) {
			top = top + sol[j] * sol[j];
		}
		return top;
	}
	double Griewank(double sol[]) {
		int j;
		double top1, top2, top;
		top = 0;
		top1 = 0;
		top2 = 1;
		for (j = 0; j < dimension; j++) {
			top1 = top1 + Math.pow((sol[j]), (double) 2);
			top2 = top2
					* Math.cos((((sol[j]) / Math.sqrt((double) (j + 1))) * Math.PI) / 180);

		}
		top = (1 / (double) 4000) * top1 - top2 + 1;
		return top;
	}
	double ackley(double[] sol){
		double top1 = 0;
		double top2 = 0;
		double val = 0;
		for(int j=0; j<dimension; j++){
			top1 += sol[j]*sol[j];
			top2 += Math.cos(2*Math.PI*sol[j]);
		}
		val = 20+Math.E - 20*Math.exp(-0.2*Math.sqrt(top1/dimension))-Math.exp(top2/dimension);
		return val;
	}
	public double Rastrigin(double sol[]) {
		int j;
		double top = 0;

		for (j = 0; j < dimension; j++) {
			top = top
					+ (Math.pow(sol[j], (double) 2) - 10
							* Math.cos(2 * Math.PI * sol[j]) + 10);
		}
		return top;
	}
	
	public double f1(double[] sol){
		double val =0;
		for(int i=0; i<dimension; i++){
			val += (i+1)*sol[i]*sol[i];
		}
		return val;
	}
	public double f2(double sol[]){
		double val =0;
		for(int i=0; i<dimension; i++){
			val += (i+1)*Math.pow(sol[i],4) ;
		}
		return val+Math.random();
	}
	public double f3(double sol[]){
		double val =0;
		for(int i=0; i<dimension; i++){
			val += Math.pow(Math.abs(sol[i]), i+2);
		}
		return val;
	}
	public double f4(double sol[]){
		double a=0;
		double b=0;
		for(int i=0; i<dimension; i++){
			a += Math.abs(sol[i]);
			b *= Math.abs(sol[i]);
		}
		return a+b;
	}
	
	
	public double f6(double sol[]){
		double val =0;
		for(int i=0; i<dimension; i++){
			val += Math.abs(sol[i]*Math.sin(sol[i])+0.1*sol[i]);
		}
		return val;
	}
	public double f7(double sol[]){
		double val =0;
		for(int i=0; i<dimension-1; i++){
			val += Math.pow(sol[i]-1, 2)*(1+Math.pow(Math.sin(3*Math.PI*sol[i+1]), 2));
		}
		return val+Math.pow(Math.sin(3*Math.PI*sol[0]), 2)+
				Math.abs(sol[dimension-1]-1)*(1+Math.pow(Math.sin(3*Math.PI*sol[dimension-1]), 2));
	}
	public double f8(double sol[]){
		double val =0;
		for(int i=0; i<dimension; i++){
			val += Math.pow(sol[i], 4)-16*Math.pow(sol[i], 2)+5*sol[i];
		}
		return val/dimension+78.33236;
	}

}
