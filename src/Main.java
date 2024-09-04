import java.util.HashMap;
import java.util.Optional;

class Human {
    private double height;
    private double weight;

    Human(double height, double weight) {
        this.height = height;
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}

interface IBodyMass {
    Number calculateMass();
}

class HumanBodyMass implements IBodyMass {
    private final double weight;
    private final double height;

    HumanBodyMass(Human human) {
        this.weight = human.getWeight();
        this.height = human.getHeight();
    }

    @Override
    public Number calculateMass() {
        // TODO: Need validation and custom errors by Domain design of clinic
        if (height <= 0) throw new RuntimeException("Weight must be greater than 0");

        return weight / (height * height);
    }
}

enum PatientType {
    MALE, MALE_CHILD, FEMALE, FEMALE_CHILD,
}

enum BodyMassIndex {
    Norm, Warning, Fat, Deficit,
}


// TODO: Add message or use it as a root Exception type
class InvalidBodyMassException extends Exception {
}

interface IPatientBodyIndexDefiner {
    BodyMassIndex definePatientBodyMassIndex(Number bodyMass) throws InvalidBodyMassException;
}

class DefaultPatientBodyIndexDefiner implements IPatientBodyIndexDefiner {
    @Override
    public BodyMassIndex definePatientBodyMassIndex(Number bodyMass) throws InvalidBodyMassException {
        return switch (bodyMass) {
            case Integer imb when imb >= 18.5 && imb < 25 -> BodyMassIndex.Norm;
            case Double imb when imb >= 18.5 && imb < 25 -> BodyMassIndex.Norm;

            case Integer imb when imb >= 25 && imb < 30 -> BodyMassIndex.Warning;
            case Double imb when imb >= 25 && imb < 30 -> BodyMassIndex.Warning;

            case Integer imb when imb >= 30 -> BodyMassIndex.Fat;
            case Double imb when imb >= 30 -> BodyMassIndex.Fat;

            case Integer imb when imb < 18.5 -> BodyMassIndex.Fat;
            case Double imb when imb < 18.5 -> BodyMassIndex.Fat;

            default -> throw new InvalidBodyMassException();
        };
    }
}

class BodyMassIndexEvaluator {
    private final IPatientBodyIndexDefiner bodyIndexDefiner;

    BodyMassIndexEvaluator(IPatientBodyIndexDefiner _bodyIndexDefiner) {
        this.bodyIndexDefiner = _bodyIndexDefiner;
    }

    public BodyMassIndex evaluate(IBodyMass bodyMass) throws InvalidBodyMassException {
        var bodyMassValue = bodyMass.calculateMass();
        if (bodyMassValue.intValue() <= 0) {
            throw new InvalidBodyMassException();
        }
        return bodyIndexDefiner.definePatientBodyMassIndex(bodyMass.calculateMass());
    }
}

class BodyMassIndexEvaluatorFactory {
    private final HashMap<PatientType, IPatientBodyIndexDefiner> bodyIndexDefiners = new HashMap<>() {{
        // TODO: Implement unique definer for each type of patient
        put(PatientType.MALE, new DefaultPatientBodyIndexDefiner());
        put(PatientType.MALE_CHILD, new DefaultPatientBodyIndexDefiner());
        put(PatientType.FEMALE, new DefaultPatientBodyIndexDefiner());
        put(PatientType.FEMALE_CHILD, new DefaultPatientBodyIndexDefiner());
    }};


    BodyMassIndexEvaluator createBodyMassIndexEvaluatorByPatientType(PatientType patientType) throws RuntimeException {
        var definer = Optional.ofNullable(bodyIndexDefiners.get(patientType)).orElseThrow(() -> new RuntimeException("Not found body evaluator for patient type"));
        return new BodyMassIndexEvaluator(definer);
    }
}

public class Main {
    final static BodyMassIndexEvaluatorFactory bodyMassIndexEvaluatorFactory = new BodyMassIndexEvaluatorFactory();

    public static void main(String[] args) throws InvalidBodyMassException, RuntimeException {
//        var human = new Human(1.90, 80); // => Norm
//        var human = new Human(1.52, 80); // => Fat
        var human = new Human(1.62, 70.5);  // => Warning
//        var human = new Human(1.62, 0);  // => InvalidBodyMassException
//        var human = new Human(0, 70.5);  // => java.lang.RuntimeException: Weight must be greater than 0

        IBodyMass humanBodyMass = new HumanBodyMass(human);
        var maleBodyMassIndexEvaluator = bodyMassIndexEvaluatorFactory.createBodyMassIndexEvaluatorByPatientType(PatientType.MALE);

        var bodyMassIndex = maleBodyMassIndexEvaluator.evaluate(humanBodyMass);
        System.out.println(bodyMassIndex);
    }
}