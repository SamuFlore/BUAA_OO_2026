package Factor;

public interface Factor {
    
    // 求导方法，var为求导变量 "x" 或 "y"
    Factor derive(String var);
    
    // 深克隆方法（思考：为什么需要深克隆？）
    Factor clone();
}
