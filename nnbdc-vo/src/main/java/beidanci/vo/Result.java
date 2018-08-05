package beidanci.vo;

public class Result<T> {
	public static Result<Object> SUCCESS = new Result<Object>(true, null, null);
	private boolean isSuccess;
	private String msg;
	private T data;

	public Result(boolean isSuccess, String msg, T data) {
		this.isSuccess = isSuccess;
		this.msg = msg;
		this.data = data;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
