package main

import "testing"

func TestConvertName(t *testing.T) {
	type args struct {
		name string
	}
	tests := []struct {
		name string
		args args
		want string
	}{
		{"test", args{"test"}, "Test"},
		{"Test", args{"Test"}, "Test"},
		{"Te_st", args{"Te_st"}, "TeSt"},
		{"te_st", args{"te_st"}, "TeSt"},
		{"TE_ST", args{"TE_ST"}, "TEST"},
		{"Te_St", args{"Te_St"}, "TeSt"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := ConvertCamelName(tt.args.name); got != tt.want {
				t.Errorf("ConvertCamelName() = %v, want %v", got, tt.want)
			}
		})
	}
}
