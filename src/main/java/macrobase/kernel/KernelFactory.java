package macrobase.kernel;

import java.util.function.Supplier;

public class KernelFactory {
    public Supplier<Kernel> supplier;

    public KernelFactory(String kernel) {
        if (kernel.equals("gaussian")) {
            supplier =  GaussianKernel::new;
        } else {
            supplier = EpaKernel::new;
        }
    }

    public Kernel get() {
        return supplier.get();
    }
}
