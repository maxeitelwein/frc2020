package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.controller.PIDController;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

public class FlyWheel {

    private TalonSRX flywheel_Motor1;
    private TalonSRX flywheel_Motor2;
    public final PIDController FlyWheelPid;

    private final double kP = 0;
    private final double kI = 0;
    private final double kD = 0;
    public  double error = 0;

    public FlyWheel(final WPI_TalonSRX motor1, final WPI_TalonSRX motor2) {

        flywheel_Motor1 = motor1;
        flywheel_Motor2 = motor2;

        error = flywheel_Motor1.getSelectedSensorVelocity();

        FlyWheelPid = new PIDController(kP, kI, kD);
    }



    public void setRPM(double targetRPM){
        FlyWheelPid.setSetpoint(targetRPM);
    }

    public void PID_FlyWheel(){
        set_FlyWheel(FlyWheelPid.calculate(error));
    }

    public void set_FlyWheel(double value)
    {
        flywheel_Motor1.set(ControlMode.PercentOutput, value);
        flywheel_Motor2.set(ControlMode.PercentOutput, value);
    }

    public void periodic() {
    }
}
