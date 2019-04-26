package com.paletter.stdy.tcg;

public class User {

	private Integer id;
	private String name;
	
	private UserService userService;

	public void test() {
		
		int i = 1;
		int i2 = 1;
		
		i = 10;
		
		if (userService.getUser().equals("")) {
			String s = "";
			System.out.println(s);
		} else if (i == 2) {
			System.out.println("===");
		} else {
			System.out.println("-------");
		}
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
