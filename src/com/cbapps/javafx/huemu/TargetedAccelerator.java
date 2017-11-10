package com.cbapps.javafx.huemu;

/**
 * @author Coen Boelhouwers
 * @version 1.0
 */
public class TargetedAccelerator {

	private double acceleration;
	private boolean brake;
	private double currentSpeed;
	private double currentValue;
	private double margin = 2;
	private double maxSpeed;
	private double targetSpeed;
	private double targetValue;

	public TargetedAccelerator(double startValue, double acceleration, double maxSpeed) {
		this.currentValue = startValue;
		setAcceleration(acceleration);
		setMaxSpeed(maxSpeed);
	}

	public void applyBrake(boolean value) {
		this.brake = value;
	}

	public double calculateStopDistance() {
		return (currentSpeed * currentSpeed) / (2 * acceleration);
	}

	public double calculateStopTime() {
		return currentSpeed / acceleration;
	}

	public double calculateTravelTime() {
		double d = calculateStopDistance();
		double t = calculateStopTime();
		if (getRemainingDistance() > d)
			return (getRemainingDistance() - d) / Math.abs(currentSpeed) + t;
		else return t;
	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}

	public double getMargin() {
		return margin;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public double getRemainingDistance() {
		return Math.abs(targetValue - currentValue);
	}

	public double getTarget() {
		return targetValue;
	}

	public double getValue() {
		return currentValue;
	}

	private void handleOvershoot(double s) {
		double v = currentSpeed;
		double t = s / v;
		double v_delta = acceleration * t;
		double s_delta = (v * t) - ((v-v_delta) * t);
		currentSpeed -= v_delta;
		currentValue -= s_delta;
	}

	public boolean isStopped() {
		return currentSpeed > -0.005 && currentSpeed < 0.005;
	}

	public boolean isTargetReached() {
		return Math.abs(targetValue - currentValue) < margin;
	}

	public void resetValue(double value){
		this.currentValue = value;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public void setMargin(double value) {
		this.margin = value;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = Math.abs(maxSpeed);
	}

	public void setTarget(double value) {
		this.targetValue = value;
	}

	public void update(double elapsedSeconds) {
		currentValue += currentSpeed * elapsedSeconds;
		if (brake) {
			targetSpeed = 0;
		} else {
			double stopDistance = calculateStopDistance();
			if (targetValue > currentValue + margin) {
				double distTillBrake = targetValue - currentValue - stopDistance;
				if (distTillBrake <= 0) {
					if (targetSpeed != 0)
						handleOvershoot(-distTillBrake);
					targetSpeed = 0;
				} else if (distTillBrake > margin) {
					//System.out.println("Distance before stopping: " + distTillBrake);
					targetSpeed = maxSpeed;
				}
			} else if (targetValue < currentValue - margin) {
				double distTillBrake = currentValue - targetValue - stopDistance;
				if (distTillBrake <= 0) {
					if (targetSpeed != 0)
						handleOvershoot(-distTillBrake);
					targetSpeed = -0;
				} else if (distTillBrake > margin)
					targetSpeed = -maxSpeed;
			}
		}
		updateSpeed(elapsedSeconds);
	}

	private void updateSpeed(double elapsedTime) {
		if (targetSpeed > currentSpeed + acceleration * elapsedTime)
			currentSpeed += acceleration * elapsedTime;
		else if (targetSpeed < currentSpeed - acceleration * elapsedTime)
			currentSpeed -= acceleration * elapsedTime;
		else currentSpeed = targetSpeed;
	}
}
