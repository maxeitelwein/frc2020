package frc.robot.subsystems;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class DriveBaseOld extends SubsystemBase {
	private final WPI_TalonSRX m_motorLeft1 = new WPI_TalonSRX(Constants.Motors.MOTOR_LEFT_1);
	private final WPI_TalonSRX m_motorLeft2 = new WPI_TalonSRX(Constants.Motors.MOTOR_LEFT_2);
	private final WPI_TalonSRX m_motorRight1 = new WPI_TalonSRX(Constants.Motors.MOTOR_RIGHT_1);
	private final WPI_TalonSRX m_motorRight2 = new WPI_TalonSRX(Constants.Motors.MOTOR_RIGHT_2);
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
	private double lockedDistance;
	private double speed = Constants.Motors.SPEED;

	public DriveBaseOld(boolean arsch) {
		m_motorLeft1.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft1.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft2.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft2.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorRight2.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorRight2.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
	}

	public DriveBaseOld() {

		/* Factory Default all hardware to prevent unexpected behaviour */
		m_motorRight1.configFactoryDefault();
		m_motorLeft1.configFactoryDefault();

		/* Set Neutral Mode */
		m_motorLeft1.setNeutralMode(NeutralMode.Brake);
		m_motorLeft2.setNeutralMode(NeutralMode.Brake);
		m_motorRight1.setNeutralMode(NeutralMode.Brake);
		m_motorRight2.setNeutralMode(NeutralMode.Brake);

		m_motorLeft1.configSelectedFeedbackSensor(	FeedbackDevice.QuadEncoder,					// Local Feedback Source
													Constants.Autonomous.PID_PRIMARY,			// PID Slot for Source [0, 1]
													Constants.Autonomous.TIMEOUT);				// Configuration Timeout

		/* Configure the Remote Talon's selected sensor as a remote sensor for the right Talon */
		m_motorRight1.configRemoteFeedbackFilter(m_motorLeft1.getDeviceID(),					// Device ID of Source
												RemoteSensorSource.TalonSRX_SelectedSensor,		// Remote Feedback Source
												Constants.Autonomous.REMOTE_0,					// Source number [0, 1]
												Constants.Autonomous.TIMEOUT);					// Configuration Timeout
		
		/* Setup Sum signal to be used for Distance */
		m_motorRight1.configSensorTerm(SensorTerm.Sum0, FeedbackDevice.RemoteSensor0, Constants.Autonomous.TIMEOUT);			// Feedback Device of Remote Talon
		m_motorRight1.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.CTRE_MagEncoder_Relative, Constants.Autonomous.TIMEOUT);	// Quadrature Encoder of current Talon
		
		/* Setup Difference signal to be used for Turn */
		m_motorRight1.configSensorTerm(SensorTerm.Diff1, FeedbackDevice.RemoteSensor0, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configSensorTerm(SensorTerm.Diff0, FeedbackDevice.CTRE_MagEncoder_Relative, Constants.Autonomous.TIMEOUT);
		
		/* Configure Sum [Sum of both QuadEncoders] to be used for Primary PID Index */
		m_motorRight1.configSelectedFeedbackSensor(	FeedbackDevice.SensorSum, 
													Constants.Autonomous.PID_PRIMARY,
													Constants.Autonomous.TIMEOUT);
		
		/* Scale Feedback by 0.5 to half the sum of Distance */
		m_motorRight1.configSelectedFeedbackCoefficient(0.5, 									// Coefficient
														Constants.Autonomous.PID_PRIMARY,		// PID Slot of Source 
														Constants.Autonomous.TIMEOUT);			// Configuration Timeout
		
		/* Configure Difference [Difference between both QuadEncoders] to be used for Auxiliary PID Index */
		m_motorRight1.configSelectedFeedbackSensor(	FeedbackDevice.SensorDifference, 
													Constants.Autonomous.PID_TURN, 
													Constants.Autonomous.TIMEOUT);
		
		/* Scale the Feedback Sensor using a coefficient */
		m_motorRight1.configSelectedFeedbackCoefficient(1,
														Constants.Autonomous.PID_TURN, 
														Constants.Autonomous.TIMEOUT);
		/* Configure output and sensor direction */
		m_motorLeft1.setInverted(true);
		m_motorLeft2.setInverted(true);
		m_motorLeft1.setSensorPhase(true);
		m_motorRight1.setInverted(false);
		m_motorRight2.setInverted(false);
		m_motorRight1.setSensorPhase(true);
		
		/* Set status frame periods to ensure we don't have stale data */
		m_motorRight1.setStatusFramePeriod(StatusFrame.Status_12_Feedback1, 20, Constants.Autonomous.TIMEOUT);
		m_motorRight1.setStatusFramePeriod(StatusFrame.Status_13_Base_PIDF0, 20, Constants.Autonomous.TIMEOUT);
		m_motorRight1.setStatusFramePeriod(StatusFrame.Status_14_Turn_PIDF1, 20, Constants.Autonomous.TIMEOUT);
		m_motorRight1.setStatusFramePeriod(StatusFrame.Status_10_Targets, 20, Constants.Autonomous.TIMEOUT);
		m_motorLeft1.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5, Constants.Autonomous.TIMEOUT);

		/* Configure neutral deadband */
		m_motorRight1.configNeutralDeadband(0.001, Constants.Autonomous.TIMEOUT);
		m_motorLeft1.configNeutralDeadband(0.001, Constants.Autonomous.TIMEOUT);
		
		/* Motion Magic Configurations */
		m_motorRight1.configMotionAcceleration(2000, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configMotionCruiseVelocity(2000, Constants.Autonomous.TIMEOUT);

		/**
		 * Max out the peak output (for all modes).  
		 * However you can limit the output of a given PID object with configClosedLoopPeakOutput().
		 */
		m_motorLeft1.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft1.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft2.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorLeft2.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);
		m_motorRight2.configPeakOutputForward(+speed, Constants.Autonomous.TIMEOUT);
		m_motorRight2.configPeakOutputReverse(-speed, Constants.Autonomous.TIMEOUT);

		/* FPID Gains for distance servo */
		m_motorRight1.config_kP(Constants.Autonomous.kSlot_Distanc, kP1, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kI(Constants.Autonomous.kSlot_Distanc, kI1, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kD(Constants.Autonomous.kSlot_Distanc, kD1, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kF(Constants.Autonomous.kSlot_Distanc, kF1, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_IntegralZone(Constants.Autonomous.kSlot_Distanc, 100, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configClosedLoopPeakOutput(Constants.Autonomous.kSlot_Distanc, 0.5, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configAllowableClosedloopError(Constants.Autonomous.kSlot_Distanc, 0, Constants.Autonomous.TIMEOUT);

		/* FPID Gains for turn servo */
		m_motorRight1.config_kP(Constants.Autonomous.kSlot_Turning, kP2, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kI(Constants.Autonomous.kSlot_Turning, kI2, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kD(Constants.Autonomous.kSlot_Turning, kD2, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_kF(Constants.Autonomous.kSlot_Turning, kF2, Constants.Autonomous.TIMEOUT);
		m_motorRight1.config_IntegralZone(Constants.Autonomous.kSlot_Turning, (int)200, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configClosedLoopPeakOutput(Constants.Autonomous.kSlot_Turning, 1, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configAllowableClosedloopError(Constants.Autonomous.kSlot_Turning, 0, Constants.Autonomous.TIMEOUT);

		int closedLoopTimeMs = 1;
		m_motorRight1.configClosedLoopPeriod(0, closedLoopTimeMs, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configClosedLoopPeriod(1, closedLoopTimeMs, Constants.Autonomous.TIMEOUT);

		/**
		 * false: talon's local output is PID0 + PID1, and other side Talon is PID0 - PID1
		 * true: talon's local output is PID0 - PID1, and other side Talon is PID0 + PID1
		 */
		m_motorRight1.configAuxPIDPolarity(false, Constants.Autonomous.TIMEOUT);

		m_motorRight1.setStatusFramePeriod(StatusFrameEnhanced.Status_10_Targets, 10);
		m_motorLeft1.getSensorCollection().setQuadraturePosition(0, Constants.Autonomous.TIMEOUT);
		m_motorRight1.getSensorCollection().setQuadraturePosition(0, Constants.Autonomous.TIMEOUT);
		m_motorRight1.configMotionSCurveStrength(smoothing);
	}

	public void setDistance(double distance) {
		targetPos = distance * 4096 / 18.85 / 2; // inches
		SmartDashboard.putNumber("target", targetPos);
	}
	public void setAngle(double angle) {
		if (angle != 0){
			turningValue = angle;
		} else {
			turningValue = m_motorRight1.getSelectedSensorPosition(1);
		}
	}

	public void drive() {
		SmartDashboard.putNumber("targetPos", targetPos);
		m_motorRight1.selectProfileSlot(0, 0);
		m_motorRight1.selectProfileSlot(1, 1);
		m_motorRight1.set(ControlMode.MotionMagic, targetPos, DemandType.AuxPID, turningValue);
		m_motorLeft1.follow(m_motorRight1, FollowerType.AuxOutput1);
		m_motorLeft2.follow(m_motorLeft1);
		m_motorRight2.follow(m_motorRight1);
		/*
		 * ShuffleboardTab AutoDriveBaseTab =
		 * Shuffleboard.getTab("AutoDriveBase_PID_Values");
		 * 
		 * AutoDriveBaseTab.add("dkP1", kP1).getEntry().addListener(notification -> {
		 * m_motorLeft1.config_kP(0, notification.value.getDouble()); },
		 * EntryListenerFlags.kUpdate); AutoDriveBaseTab.add("dkI1",
		 * kI1).getEntry().addListener(notification -> { m_motorLeft1.config_kI(0,
		 * notification.value.getDouble()); }, EntryListenerFlags.kUpdate);
		 * AutoDriveBaseTab.add("dkD1", kD1).getEntry().addListener(notification -> {
		 * m_motorLeft1.config_kD(0, notification.value.getDouble()); },
		 * EntryListenerFlags.kUpdate); AutoDriveBaseTab.add("dkF1",
		 * kF1).getEntry().addListener(notification -> { m_motorLeft1.config_kF(0,
		 * notification.value.getDouble()); }, EntryListenerFlags.kUpdate);
		 * 
		 * AutoDriveBaseTab.add("dkP2", kP2).getEntry().addListener( notification ->
		 * {m_motorLeft1.config_kP(0, notification.value.getDouble());
		 * },EntryListenerFlags.kUpdate); AutoDriveBaseTab.add("dkI2",
		 * kI2).getEntry().addListener( notification -> {m_motorLeft1.config_kI(0,
		 * notification.value.getDouble()); },EntryListenerFlags.kUpdate);
		 * AutoDriveBaseTab.add("dkD2", kD2).getEntry().addListener( notification ->
		 * {m_motorLeft1.config_kD(0, notification.value.getDouble());
		 * },EntryListenerFlags.kUpdate); AutoDriveBaseTab.add("dkF2",
		 * kF2).getEntry().addListener( notification -> {m_motorLeft1.config_kF(0,
		 * notification.value.getDouble()); },EntryListenerFlags.kUpdate);
		 */
	}

	public void readSensors() {
		SmartDashboard.putNumber("Sensor left", m_motorLeft1.getSelectedSensorVelocity());
		SmartDashboard.putNumber("Sensor right", m_motorRight1.getSelectedSensorVelocity());
	}

	@Override
	public void periodic() {
	}

	public void stop() {
		m_motorLeft1.set(ControlMode.PercentOutput, 0);
		m_motorLeft2.set(ControlMode.PercentOutput, 0);
		m_motorRight1.set(ControlMode.PercentOutput, 0);
		m_motorRight2.set(ControlMode.PercentOutput, 0);
	}

	public void tankDrive(final double speedLeft, final double speedRight) {
		m_motorLeft1.set(ControlMode.PercentOutput, speedLeft * m_motorLeft1.getSupplyCurrent()/134);
		m_motorLeft2.follow(m_motorLeft1);
		m_motorRight1.set(ControlMode.PercentOutput, speedRight);
		m_motorRight2.follow(m_motorRight1);
		System.out.println(m_motorLeft1.getSupplyCurrent());
	}
}
