package frc.robot.subsystems;

import com.analog.adis16448.frc.ADIS16448_IMU;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class AutoDriveBase extends SubsystemBase{
    private final WPI_TalonSRX m_motorLeft1, m_motorLeft2, m_motorRight1, m_motorRight2;
    private final ADIS16448_IMU m_imu;
    private final double kP1 = 0.2;
    private final double kI1 = 0;
    private final double kD1 = 0.2;
    private final double kF1 = 0;
    private final double kP2 = 2.0;
    private final double kI2 = 0;
    private final double kD2 = 4.0;
    private final double kF2 = 0;
    private final int smoothing = 4;
    private double targetPos;
    private double turningValue;

    public AutoDriveBase(final WPI_TalonSRX motorLeft1, final WPI_TalonSRX motorLeft2, final WPI_TalonSRX motorRight1,
                        final WPI_TalonSRX motorRight2, ADIS16448_IMU imu) {
                        
        m_motorLeft1 = motorLeft1;
        m_motorLeft2 = motorLeft2;
        m_motorRight1 = motorRight1;
        m_motorRight2 = motorRight2;
        m_imu = imu;

        m_motorLeft1.configFactoryDefault();

        m_motorLeft1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 30);
        
        m_motorLeft1.configNeutralDeadband(0.001, 30);
        
		m_motorLeft1.setSensorPhase(false);
        m_motorLeft1.setInverted(false);
        
		m_motorLeft1.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, 30);
        m_motorLeft1.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 30);
        
		m_motorLeft1.configNominalOutputForward(0, 30);
		m_motorLeft1.configNominalOutputReverse(0, 30);
		m_motorLeft1.configPeakOutputForward(1, 30);
        m_motorLeft1.configPeakOutputReverse(-1, 30);
        
		m_motorLeft1.selectProfileSlot(0, 0);
		m_motorLeft1.config_kF(0, kF1, 30);
		m_motorLeft1.config_kP(0, kP1, 30);
		m_motorLeft1.config_kI(0, kI1, 30);
        m_motorLeft1.config_kD(0, kD1, 30);
        
		m_motorLeft1.configMotionCruiseVelocity(15000, 30);
        m_motorLeft1.configMotionAcceleration(6000, 30);
        
        m_motorLeft1.setSelectedSensorPosition(0, 0, 30);
        m_motorLeft1.configMotionSCurveStrength(smoothing);
    }
    public void setDistance(double distance){
        targetPos =  Constants.Measurements.WHEEL_MOVE_TICK * 217.391 * distance; //inches
    }
    public void motionMagicDrive(){
        m_motorLeft1.set(ControlMode.MotionMagic, targetPos);
        m_motorLeft2.follow(m_motorLeft1);
        m_motorRight1.follow(m_motorLeft1);
        m_motorRight2.follow(m_motorLeft1);
    }
}